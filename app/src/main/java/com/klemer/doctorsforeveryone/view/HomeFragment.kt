package com.klemer.doctorsforeveryone.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.klemer.doctorsforeveryone.R
import com.klemer.doctorsforeveryone.adapter.CategoryAdapter
import com.klemer.doctorsforeveryone.adapter.DoctorAdapter
import com.klemer.doctorsforeveryone.databinding.HomeFragmentBinding
import com.klemer.doctorsforeveryone.model.Category
import com.klemer.doctorsforeveryone.model.Doctor
import com.klemer.doctorsforeveryone.model.User
import com.klemer.doctorsforeveryone.utils.hideKeyboard
import com.klemer.doctorsforeveryone.view_model.CategoryViewModel
import com.klemer.doctorsforeveryone.view_model.DoctorViewModel
import com.klemer.doctorsforeveryone.view_model.HomeViewModel

class HomeFragment : Fragment(R.layout.home_fragment) {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var viewModelCategory: CategoryViewModel
    private lateinit var viewModelDoctor: DoctorViewModel
    private lateinit var binding: HomeFragmentBinding

    private var adapterCategory = CategoryAdapter {
        viewModelDoctor.fetchDoctorByCategory(it.name)
    }
    private var adapterDoctor = DoctorAdapter {
        showBottomSheetDialog(it)
    }

    private val observerCategoryGetAll = Observer<List<Category>> {
        adapterCategory.refresh(it)
    }

    private val observerDoctorGetALL = Observer<List<Doctor>> {
        if (it != null) {
            adapterDoctor.refresh(it)
        } else {
            adapterDoctor.clear()
            Snackbar.make(requireView(), "Nao foi encontrado Nenhum Doctor!", Snackbar.LENGTH_LONG)
                .show()
        }
    }

    private val currentUserObserver = Observer<User> {
        binding.headerFragment.nameUser.text = "Olá ${it.name}"
    }

    override fun onStart() {
        super.onStart()
        viewModelCategory.fetchCategory()
        viewModelDoctor.fetchDoctor()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadComponents(view)
        setupOservers()
        executeComponents()
    }

    private fun loadComponents(view2: View) {
        binding = HomeFragmentBinding.bind(view2)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModelCategory = ViewModelProvider(this).get(CategoryViewModel::class.java)
        viewModelDoctor = ViewModelProvider(this).get(DoctorViewModel::class.java)
    }

    private fun setupOservers() {
        viewModelCategory.categoryGet.observe(viewLifecycleOwner, observerCategoryGetAll)
        viewModelDoctor.doctorGet.observe(viewLifecycleOwner, observerDoctorGetALL)
        viewModel.currentUser.observe(viewLifecycleOwner, currentUserObserver)
    }

    private fun executeComponents() {
        viewModel.getCurrentUser()
        //Lista de categorias
        setupRecyclerViewCategory()
        //Lista de médicos
        setupRecyclerViewDoctor()

        binding.headerFragment.includeSearch.searchDoctors.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let {
                    if (it.length > 2) {
                        viewModelDoctor.fetchDoctorByName(it.toString())
                    } else if (it.length == 0) {
                        viewModelDoctor.fetchDoctor()
                    }
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun setupRecyclerViewCategory() = with(binding.include.recyclerViewCategory){
        //Lista de categorias
        adapter = adapterCategory
        layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                requireActivity().hideKeyboard()
            }
        })
    }

    private fun setupRecyclerViewDoctor() = with(binding.recyclerViewDoctorsList) {
        //Lista de médicos
        layoutManager = LinearLayoutManager(requireContext())
        adapter = adapterDoctor

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                requireActivity().hideKeyboard()
            }
        })
    }

    private fun showBottomSheetDialog(doctor: Doctor) {
        val bottomSheet = BottomSheetFragment.newInstance()
        val arguments = Bundle()
        arguments.putSerializable("doctor", doctor)
        bottomSheet.arguments = arguments
        bottomSheet.show(parentFragmentManager, "dialog_doctors")
    }
}