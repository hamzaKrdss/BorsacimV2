package com.example.borsacimv1.classes

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.borsacimv1.R
import android.view.animation.AnimationUtils

class HomeFragment : Fragment() {

    private lateinit var dailyShare: Button
    private lateinit var weekShare: Button
    private lateinit var monthShare: Button

    private lateinit var buttonList: List<Button>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dailyShare = view.findViewById(R.id.dailShare)
        weekShare = view.findViewById(R.id.weekShare)
        monthShare = view.findViewById(R.id.monthShare)

        buttonList = listOf(dailyShare, weekShare, monthShare)

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?) = true
        })
        searchView.clearFocus()

        selectButton(dailyShare)

        buttonList.forEach { button ->
            button.setOnClickListener {
                selectButton(button)
                val anim = AnimationUtils.loadAnimation(context, R.anim.button_click)
                button.startAnimation(anim)
                // Şimdilik buton tıklama işleminde başka bir şey yok
            }
        }

        return view
    }

    private fun selectButton(selectedButton: Button) {
        buttonList.forEach { it.isSelected = false }
        selectedButton.isSelected = true
    }
}
