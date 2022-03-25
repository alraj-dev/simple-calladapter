package dev.alraj.simplecalladaptersample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import dev.alraj.simplecalladapter.MultipleCall
import dev.alraj.simplecalladapter.SimpleCallback
import timber.log.Timber
import java.lang.reflect.Type
import kotlin.reflect.typeOf

class MainActivity : AppCompatActivity() {
    private var fragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        json()
        findViewById<Button>(R.id.add).setOnClickListener { addFragment() }
        findViewById<Button>(R.id.remove).setOnClickListener { removeFragment() }
    }

    private fun addFragment() {
        if (fragment != null) return
        fragment = Fragment1()
        supportFragmentManager.beginTransaction()
            .add(R.id.container, fragment!!, "Fragment1")
            .commit()
    }

    private fun removeFragment() {
        fragment ?: return
        supportFragmentManager.beginTransaction()
            .remove(fragment!!)
            .commit()
        fragment = null
    }

    private inline fun <reified  T>getType(): Type {
        return object: TypeToken<Base<T>>() {}.type
    }

    private fun json() {
        RetrofitFactory.retrofitService.user()
            .enqueue { response, exception, _ ->
                exception?.let {
                    Timber.e(it)
                }
                response?.let {
                    Timber.d("$it")
                }
            }

        RetrofitFactory.retrofitService.users()
            .enqueue { response, exception, _ ->
                exception?.let {
                    Timber.e(it)
                }
                response?.let {
                    Timber.d("$it")
                }
            }

        RetrofitFactory.retrofitService.cat()
            .enqueue { response, exception, _ ->
                exception?.let {
                    Timber.e(it)
                }
                response?.let {
                    Timber.d("$it")
                }
            }

        RetrofitFactory.retrofitService.software()
            .enqueue { response, exception, _ ->
                exception?.let {
                    Timber.e(it)
                }
                response?.let {
                    Timber.d("$it")
                }
            }

        RetrofitFactory.retrofitService.success()
            .enqueue { response, exception, _ ->
                exception?.let {
                    Timber.e(it)
                }
                response?.let {
                    Timber.d("$it")
                }
            }
    }
}