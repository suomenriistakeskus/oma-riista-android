package fi.riista.mobile.feature.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import fi.riista.mobile.R

class EmailChangedFragment : Fragment() {
    interface Manager {
        fun startRegistration()
        fun returnToLogin()
    }

    private lateinit var manager: Manager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_email_changed, container, false)

        view.findViewById<MaterialButton>(R.id.btn_start_registration).also { button ->
            button.setOnClickListener {
                manager.startRegistration()
            }
        }
        view.findViewById<MaterialButton>(R.id.btn_return_to_login).also { button ->
            button.setOnClickListener {
                manager.returnToLogin()
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        manager = context as Manager
    }
}
