
import okhttp3.*
import timber.log.Timber
import java.io.IOException

/**
 * Created By   Deepak<br></br>
 * Created Date 14/12/2021<br></br><br></br>
 * Interceptor Interface that Logs all service calls
 * with url , request params & responseBody
 * works only in Debug Mode<br></br><br></br>
 *
 *
 */
class ServiceInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // Request
        val originalRequest: Request = chain.request()
        Timber.d("%s %s", originalRequest.method, originalRequest.url)
        Timber.d("Request Header: %s", originalRequest.headers)
        Timber.d("Request Body : %s", requestBodyToString(originalRequest.body))

        // Response
        var response: Response = chain.proceed(originalRequest)
        run {
            val bodyString: String = response.body?.string() ?: ""


            //UnComment to Log the headers
            /*Headers headers = originalRequest.headers();
            if(headers!=null && headers.size()>0)
            {
                for(int i=0;i<headers.size();i++)
                    Log.d(tag,headers.name(i)+" : "+headers.value(i));
            }
            */

            //Logging Response
            Timber.d("<-- %s %s %s", response.code.toString(), response.message,
                originalRequest.url
            )
//            Timber.d("Response Header: %s", response.headers())
            Timber.d( "Response Body : $bodyString")

            //UnComment Code to log Request Time in seconds
//            Timber.d("Request Time : %s secs",
//                (((response.networkResponse()?.receivedResponseAtMillis() ?: 0L)
//                        - (response.networkResponse()?.sentRequestAtMillis() ?: 0L)) * 1.0 / 1000).toString())
            //UnComment Code to log Request Time in milliseconds
            //Log.d(tag,"Request Time Millis : "+(response.receivedResponseAtMillis()-response.sentRequestAtMillis()));
            response = response.newBuilder().body(ResponseBody.create(response.body?.contentType(), bodyString)).build()
        }
        return response
    }

    private fun requestBodyToString(requestBody: RequestBody?): String {
        var reqBody = "No Request Body."
        var buffer: okio.Buffer? = null
        try {
            buffer = okio.Buffer()
            if (requestBody != null) {
                requestBody.writeTo(buffer)
                reqBody = buffer.readUtf8()
                buffer.close()
            }
        } catch (e: Exception) {
            buffer?.close()
            reqBody = "Error Converting Request Body : ${e.message}"
        } finally {
            buffer?.close()
        }
        return reqBody
    }
}