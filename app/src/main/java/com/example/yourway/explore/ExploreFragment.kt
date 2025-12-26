package com.example.yourway.explore

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.fragment.app.FragmentContainerView
import com.example.yourway.R
import com.example.yourway.Toast
import com.example.yourway.explore.postimagegrid.PostImageGridRVFragment
import com.example.yourway.explore.userlist.UserListFragment

class ExploreFragment : Fragment() {
    private lateinit var spiChoice: Spinner
    private lateinit var etSearch:EditText
    private lateinit var fcvExplore:FragmentContainerView
    private lateinit var ibtnCancel:ImageButton

    private val handler = Handler()
    private var searchRunnable: Runnable? = null
    private var currentSearchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_explore, container, false)

        spiChoice = view.findViewById(R.id.spi_explore_choice)
        etSearch = view.findViewById(R.id.et_explore_search)
        fcvExplore = view.findViewById(R.id.fcv_explore)
        ibtnCancel = view.findViewById(R.id.btn_explore_search_cancel)

        setupChoiceSpinner()

        loadPostImageGrid()

        ibtnCancel.setOnClickListener{
            etSearch.setText("")
            loadPostImageGrid()
            ibtnCancel.visibility = View.GONE

            // Remove any pending search requests
            searchRunnable?.let { handler.removeCallbacks(it) }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Show or hide the cancel button based on input
                if (etSearch.text.isEmpty()) {
                    // Reload PostImageGrid if search is cleared
                    ibtnCancel.visibility = View.GONE
                    loadPostImageGrid()
                    return
                } else {
                    displayCancelButton()
                }

                // Remove any previously scheduled searches to avoid duplicates
                searchRunnable?.let { handler.removeCallbacks(it) }



                // Set a new delayed search
                searchRunnable = Runnable {
                    currentSearchText = s.toString().trim()
                    if (spiChoice.selectedItem.toString() == "People") {

                        loadUserListFragment(currentSearchText)
                    }
                }

                // Execute the search after a 2-second delay
                handler.postDelayed(searchRunnable!!, 300)
            }
        })

        spiChoice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spiChoice.selectedItem == "people") {
                    loadUserListFragment(currentSearchText)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        return view
    }

    private fun displayCancelButton() {
        ibtnCancel.visibility = View.VISIBLE
    }

    private fun loadPostImageGrid() {
        val postImageGridFragment = PostImageGridRVFragment()

        // Start a fragment transaction and replace the container with the fragment
        childFragmentManager.beginTransaction()
            .replace(R.id.fcv_explore, postImageGridFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupChoiceSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.exploreSpinner,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spiChoice.adapter = adapter
        }

        spiChoice.setSelection(0)
    }
    private fun loadUserListFragment(searchText: String) {
        Toast("Fragment Call Requested",requireContext())
        val fragment = UserListFragment().apply {
            arguments = Bundle().apply {
                putString("searchText", searchText)
            }
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.fcv_explore, fragment)
            .addToBackStack(null)
            .commit()
    }

}