package ru.softwarefree.atriumavto.ui.consumption

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ru.softwarefree.atriumavto.R
import ru.softwarefree.atriumavto.databinding.FragmentHomeBinding
import ru.softwarefree.atriumavto.ui.data.ConsumptionAdapter
import ru.softwarefree.atriumavto.ui.data.GoogleSheetsConfig
import ru.softwarefree.atriumavto.ui.data.NetworkUtil
import ru.softwarefree.atriumavto.ui.data.NetworkUtil.showCenteredSnackbar

class ConsumptionFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var consumptionViewModel: ConsumptionViewModel
    private lateinit var adapter: ConsumptionAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.garageTitle.text = "Норма расхода топлива"

        val progressBar = binding.progressBar
        progressBar.visibility = View.VISIBLE

        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            showCenteredSnackbar(view)
        }

        consumptionViewModel = ViewModelProvider(this).get(ConsumptionViewModel::class.java)

        adapter = ConsumptionAdapter(emptyList()) { consumption ->
            val bundle = Bundle().apply {
                putParcelable("consumption", consumption)
            }
            findNavController().navigate(R.id.action_nav_consumption_to_consumptionDetailFragment, bundle)
        }

        val recyclerView: RecyclerView = binding.recyclerViewGarage
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        recyclerView.adapter = adapter

        consumptionViewModel.consumption.observe(viewLifecycleOwner, Observer { consumption ->
            adapter.updateConsumption(consumption)
            progressBar.visibility = View.GONE
        })

        binding.searchView.queryHint = "Поиск по марке автомобиля"
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filterByBrand(newText.orEmpty())
                return true
            }
        })

        val spreadsheetId = GoogleSheetsConfig.SPREADSHEET_ID
        val range = GoogleSheetsConfig.RANGE2
        val apiKey = GoogleSheetsConfig.API_KEY

        consumptionViewModel.fetchConsumptionData(spreadsheetId, range, apiKey)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}