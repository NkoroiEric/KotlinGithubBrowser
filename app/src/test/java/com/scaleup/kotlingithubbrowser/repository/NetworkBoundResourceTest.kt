package com.scaleup.kotlingithubbrowser.repository

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.scaleup.kotlingithubbrowser.AppExecutors
import com.scaleup.kotlingithubbrowser.api.ApiResponse
import com.scaleup.kotlingithubbrowser.util.ApiUtil
import com.scaleup.kotlingithubbrowser.util.CountingAppExecutors
import com.scaleup.kotlingithubbrowser.util.InstantAppExecutors
import com.scaleup.kotlingithubbrowser.vo.Resource
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import org.mockito.Mockito.*
import retrofit2.Response
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function


@RunWith(Parameterized::class)
class NetworkBoundResourceTest(useRealExecutorRule: Boolean) {
    @Rule @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveCallResult : Function<Foo,Nothing?>
    private lateinit var shouldFetch : Function<Foo?,Boolean>
    private lateinit var createCall : Function<Any?, LiveData<ApiResponse<Foo>>>
    private lateinit var networkBoundResource : NetworkBoundResource<Foo, Foo>

    private var useRealExecutors: Boolean = useRealExecutorRule

    private lateinit var countingAppExecutor: CountingAppExecutors

    private val fetchedOnce = AtomicBoolean(false)

    private val dbData = MutableLiveData<Foo>()

    val observer = Mockito.mock(Observer::class.java)


    init {
        if (useRealExecutorRule){
            countingAppExecutor = CountingAppExecutors()
        }
    }

    @Before
    fun init() {
        val appExectors: AppExecutors = if (useRealExecutors) countingAppExecutor.getAppExecutors()
        else InstantAppExecutors()
        networkBoundResource = object : NetworkBoundResource<Foo, Foo>(appExectors){
            override fun saveCallResult(item: Foo?) {
                saveCallResult.apply(item!!)
            }

            override fun createCall(): LiveData<ApiResponse<Foo>> {
                return createCall.apply(null)
            }

            override fun shouldFetch(data : Foo?): Boolean {
                return shouldFetch.apply(data) && fetchedOnce.compareAndSet(false,true)
            }

            override fun loadFromDb(): LiveData<Foo> {
                return dbData
            }
        }
    }

    fun drain(){
        if (!useRealExecutors){
            return
        }
        try {
            countingAppExecutor.drainTasks(1, TimeUnit.SECONDS)
        }catch (e : Throwable){
            throw AssertionError(e)
        }
    }

    @Test
    fun basicFromNetwork() {
        val saved = AtomicReference<Foo>()
        shouldFetch = Function { obj -> Objects.isNull(obj) }
        val fetchedDbValue = Foo(1)
        saveCallResult = Function<Foo, Nothing?> { foo ->
            saved.set(foo)
            dbData.setValue(fetchedDbValue)
            null
        }
        val networkResult = Foo(1)
        createCall = Function<Any?, LiveData<ApiResponse<Foo>>> {
            ApiUtil.createCall(Response.success(networkResult))
        }

        val observer = Mockito.mock(Observer::class.java)
        networkBoundResource.asLiveData().observeForever(observer as Observer<Resource<Foo>>)
        drain()
        verify(observer).onChanged(Resource.loading(null))
        reset(observer)
        dbData.value = null
        drain()
        assertThat(saved.get(), `is`(networkResult))
        verify(observer).onChanged(Resource.success(fetchedDbValue))
    }


    @Test fun failureFromNetwork(){
        val saved = AtomicBoolean(false)
        shouldFetch = Function { Objects.isNull(it) }
        saveCallResult = Function { foo ->
            saved.set(true)
            null
        }
        val body = ResponseBody.create(MediaType.parse("text/html"), "error")
        createCall = Function { nothing -> ApiUtil.createCall(Response.error(500,body)) }

        networkBoundResource.asLiveData().observeForever(observer as Observer<Resource<Foo>>)
        drain()
        verify(observer).onChanged(Resource.loading(null))
        reset(observer)
        dbData.value = null
        drain()
        assertThat(saved.get(), `is`(false))
        verify(observer).onChanged(Resource.error("error", null))
        verifyNoMoreInteractions(observer)
    }

    @Test fun dbSuccessWithoutNetwork(){
        val saved = AtomicBoolean(false)
        shouldFetch = Function { Objects.isNull(it) }
        saveCallResult = Function { foo ->
            saved.set(true)
            null
        }
        networkBoundResource.asLiveData().observeForever(observer as Observer<Resource<Foo>>)
        drain()
        verify(observer).onChanged(Resource.loading(null))
        reset(observer)
        val dbFoo = Foo(1)
        dbData.value = dbFoo
        drain()
        verify(observer).onChanged(Resource.success(dbFoo))
        assertThat(saved.get(), `is`(false))
        val dbFoo1 = Foo(2)
        dbData.value = dbFoo1
        drain()
        verify(observer).onChanged(Resource.success(dbFoo1))
        verifyNoMoreInteractions(observer)
    }

    @Test fun dbSuccessWithFetchFailure(){
        val dbValue = Foo(1)
        val saved = AtomicBoolean(false)
        shouldFetch = Function { it == dbValue }
        saveCallResult = Function { foo ->
            saved.set(true)
            null
        }
        val body = ResponseBody.create(MediaType.parse("text/html"), "error")
        val apiResponseLiveData = MutableLiveData<ApiResponse<Foo>>()
        createCall = Function { v -> apiResponseLiveData }

        networkBoundResource.asLiveData().observeForever(observer as Observer<Resource<Foo>>)
        drain()
        verify(observer).onChanged(Resource.loading(null))
        reset(observer)

        dbData.value = dbValue
        drain()
        verify(observer).onChanged(Resource.loading(dbValue))

        apiResponseLiveData.value = ApiResponse(Response.error(400, body))
        drain()
        assertThat(saved.get(), `is`(false))
        verify(observer).onChanged(Resource.error("error", dbValue))

        val dbValue2 = Foo(2)
        dbData.value = dbValue2
        drain()
        verify(observer).onChanged(Resource.error("error",dbValue2))
        verifyNoMoreInteractions(observer)
    }

    @Test fun dbSuccessWithReFetchSuccess(){
        val dbValue = Foo(1)
        val dbValue2 = Foo(2)
        val saved = AtomicReference<Foo>()
        shouldFetch = Function { it == dbValue}
        saveCallResult = Function { foo ->
            saved.set(foo)
            dbData.value = dbValue2
            null
        }
        val apiResponseLiveData = MutableLiveData<ApiResponse<Foo>>()
        createCall = Function { apiResponseLiveData }

        networkBoundResource.asLiveData().observeForever(observer as Observer<Resource<Foo>>)
        drain()
        verify(observer).onChanged(Resource.loading(null))
        reset(observer)

        dbData.value = dbValue
        drain()
        val networkResult = Foo(1)
        verify(observer).onChanged(Resource.loading(dbValue))
        apiResponseLiveData.value = ApiResponse(Response.success(networkResult))
        drain()
        assertThat(saved.get(), `is`(networkResult))
        verify(observer).onChanged(Resource.success(dbValue2))
        verifyNoMoreInteractions(observer)
    }


    companion object {
        class Foo(valu : Int) {
            val value = valu
        }

        @Parameterized.Parameters @JvmStatic
        fun param() : List<Boolean>{
            return listOf(true,false)
        }
    }


}