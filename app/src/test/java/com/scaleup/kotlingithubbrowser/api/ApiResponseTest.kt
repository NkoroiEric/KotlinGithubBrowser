package com.scaleup.kotlingithubbrowser.api


import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Response

@RunWith(JUnit4::class)
class ApiResponseTest {
    @Test fun exception() {
        val exception = Exception("foo")
        val apiResponse: ApiResponse<String> = ApiResponse(exception)
        assertThat(apiResponse.links, notNullValue())
        assertThat(apiResponse.body, nullValue())
        assertThat(apiResponse.code, `is`(500))
        assertThat(apiResponse.errorMessage, `is`("foo"))
    }

    @Test fun success(){
        val apiResponse = ApiResponse(Response.success("foo"))
        assertThat(apiResponse.errorMessage, nullValue())
        assertThat(apiResponse.code, `is`(200))
        assertThat(apiResponse.body, `is`("foo"))
        assertThat(apiResponse.nextPage, `is`(nullValue()))
    }

    @Test fun link(){
        val link = "<https://api.github.com/search/repositories?q=foo&page=2>; rel=\"next\"," + " <https://api.github.com/search/repositories?q=foo&page=34>; rel=\"last\""
        val headers = okhttp3.Headers.of("link", link)
        val response = ApiResponse<String>(Response.success("foo",headers))
        assertThat(response.nextPage, `is`(2))
    }

    @Test fun badPageNumber(){
        val link = "<https://api.github.com/search/repositories?q=foo&page=dsa>; rel=\"next\""
        val headers = okhttp3.Headers.of("link", link)
        val response = ApiResponse(Response.success("foo", headers))
        assertThat(response.nextPage, nullValue())
    }

    @Test fun badLinkNumber(){
        val link = "<https://api.github.com/search/repositories?q=foo&page=dsa>; relx=\"next\""
        val headers = okhttp3.Headers.of("link", link)
        val response = ApiResponse(Response.success("foo", headers))
        assertThat(response.nextPage, nullValue())
    }

    @Test fun error(){
        val response = ApiResponse<String>(Response.error(400,
                ResponseBody.create(MediaType.parse("application/txt"),"blah")))
        assertThat(response.code, `is`(400))
        assertThat(response.errorMessage, `is`("blah"))
    }
}