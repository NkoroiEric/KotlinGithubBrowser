package com.scaleup.kotlingithubbrowser.repository


import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import com.scaleup.kotlingithubbrowser.api.GithubService
import com.scaleup.kotlingithubbrowser.db.GithubDb
import com.scaleup.kotlingithubbrowser.db.RepoDao
import com.scaleup.kotlingithubbrowser.vo.Resource
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.mock

@RunWith(JUnit4::class)
class FetchNextSearchPageTaskTest {
    @Rule @JvmField val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var service : GithubService
    private lateinit var db : GithubDb
    private lateinit var repoDao : RepoDao
    private lateinit var task : FetchNextSearchPageTask
    private lateinit var value : LiveData<Resource<Boolean>>
    private lateinit var observer : Observer<Boolean>

    @Before fun init(){
        service = mock(GithubService::class.java)
        db = mock(GithubDb::class.java)
        repoDao = mock(RepoDao::class.java)


    }

}