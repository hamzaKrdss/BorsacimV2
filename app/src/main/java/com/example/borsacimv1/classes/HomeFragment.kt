package com.example.borsacimv1.classes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.SearchView
import androidx.fragment.app.Fragment

import com.example.borsacimv1.R

class HomeFragment : Fragment() {

    private lateinit var btnTopGain: Button
    private lateinit var btnTopLos: Button
    private lateinit var btnTopPop: Button
    private lateinit var btnTopFav: Button

    private lateinit var buttonList: List<Button>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        btnTopGain = view.findViewById(R.id.btnTopGainers)
        btnTopLos = view.findViewById(R.id.btnTopLosers)
        btnTopPop = view.findViewById(R.id.btnPopular)
        btnTopFav = view.findViewById(R.id.btnFav)

        buttonList = listOf(btnTopGain, btnTopLos, btnTopPop, btnTopFav)

        val searchView = view.findViewById<SearchView>(R.id.searchView)
        // Eğer arama işlevi varsa, burada yazabilirsin. Şimdilik boş bırakıyorum.
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?) = true
        })

        searchView.clearFocus()

        selectButton(btnTopGain)

        buttonList.forEach { button ->
            button.setOnClickListener {
                selectButton(button)
                val anim = AnimationUtils.loadAnimation(context, R.anim.button_click)
                button.startAnimation(anim)
                // Burada buton tıklamalarına göre yapılacak işlemler şimdilik yok
            }
        }

        return view
    }

    private fun selectButton(selectedButton: Button) {
        buttonList.forEach { it.isSelected = false }
        selectedButton.isSelected = true
    }
}
