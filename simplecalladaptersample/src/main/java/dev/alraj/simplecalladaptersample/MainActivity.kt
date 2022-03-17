package dev.alraj.simplecalladaptersample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.Fragment
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private var fragment: Fragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.plant(Timber.DebugTree())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.add).setOnClickListener { addFragment() }
        findViewById<Button>(R.id.remove).setOnClickListener { removeFragment() }
    }

    private fun addFragment() {
        if(fragment != null) return
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
}