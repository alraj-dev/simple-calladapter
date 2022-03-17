package dev.alraj.simplecalladaptersample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dev.alraj.simplecalladapter.SimpleCall
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class Fragment1 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RetrofitFactory.retrofitService.getSuccess()
            .lifecycle(viewLifecycleOwner)
            .enqueue { response, exception, _ ->
            view.findViewById<TextView>(R.id.ftext).text = response?.result
                ?: exception?.let { it::class.simpleName }
                        ?: "nothing"
        }
    }
}