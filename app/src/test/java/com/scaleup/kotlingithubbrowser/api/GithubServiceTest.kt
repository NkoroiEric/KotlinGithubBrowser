package com.scaleup.kotlingithubbrowser.api

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.text.style.TabStopSpan
import com.scaleup.kotlingithubbrowser.util.LiveDataCallAdapterFactory
import com.scaleup.kotlingithubbrowser.util.LiveDataTestUtil
import com.scaleup.kotlingithubbrowser.util.LiveDataTestUtil.getValue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Okio
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.charset.StandardCharsets
import java.util.*

@RunWith(JUnit4::class)
class GithubServiceTest {
    @Rule @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service : GithubService
    private lateinit var mockWebServer : MockWebServer

    @Before @Throws(IOException::class)
    fun createService(){
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .build()
                .create(GithubService::class.java)
    }

    @After fun stopService(){
        mockWebServer.shutdown()
    }

    @Test @Throws(IOException::class, InterruptedException::class)
    fun getUser(){
        enqueueResponse("user-yigit.json");
        val user = LiveDataTestUtil.getValue(service.getUser("yigit")).body

        val request = mockWebServer.takeRequest()
        assertThat(request.path, `is`("/users/yigit"))

        assertThat(user, notNullValue())
        assertThat(user!!.avatarUrl, `is`("https://avatars3.githubusercontent.com/u/89202?v=3"))
        assertThat(user.company, `is`("Google"))
        assertThat(user.blog, `is`("birbit.com"))
    }


    @Test @Throws(IOException::class, InterruptedException::class)
    fun getRepos() {
        enqueueResponse("repos-yigit.json")
        val repos = getValue(service.getRepos("yigit")).body

        val request = mockWebServer.takeRequest()
        assertThat(request.path, `is`("/users/yigit/repos"))

        assertThat(repos!!.size, `is`(2))

        val repo = repos[0]
        assertThat(repo.fullName, `is`("yigit/AckMate"))

        val owner = repo.owner
        assertThat(owner, notNullValue())
        assertThat(owner.login, `is`("yigit"))
        assertThat(owner.url, `is`("https://api.github.com/users/yigit"))

        val repo2 = repos[1]
        assertThat(repo2.fullName , `is`("yigit/android-architecture"))
    }

    private fun enqueueResponse(filename: String) {
        enqueueResponse(filename, mutableMapOf<String, String>())
    }

    private fun enqueueResponse(filename: String, headers : MutableMap<String, String>){
        val inputStream = javaClass.classLoader.getResourceAsStream("api-response/" + filename)
        val source = Okio.buffer(Okio.source(inputStream))
        val mockResponse = MockResponse()
        for (header in headers.entries){
            mockResponse.addHeader(header.key, header.value)
        }
        mockWebServer.enqueue(mockResponse.setBody(source.readString(StandardCharsets.UTF_8)))
    }
}