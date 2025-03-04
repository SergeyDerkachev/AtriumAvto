package ru.softwarefree.atriumavto.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ru.softwarefree.atriumavto.R
import ru.softwarefree.atriumavto.databinding.FragmentHomeBinding
import ru.softwarefree.atriumavto.ui.data.GoogleSheetsConfig
import ru.softwarefree.atriumavto.ui.data.NetworkUtil
import ru.softwarefree.atriumavto.ui.data.NetworkUtil.showCenteredSnackbar
import ru.softwarefree.atriumavto.ui.data.VehicleAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: VehicleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.garageTitle.text = "Автотранспорт Атриум"

        val progressBar = binding.progressBar
        progressBar.visibility = View.VISIBLE

        if (!NetworkUtil.isInternetAvailable(requireContext())) {
            showCenteredSnackbar(view)
        }

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        adapter = VehicleAdapter(emptyList()) { vehicle ->
            val bundle = Bundle().apply {
                putParcelable("vehicle", vehicle)
            }
            findNavController().navigate(R.id.action_nav_home_to_vehicleDetailFragment, bundle)
        }

        val recyclerView: RecyclerView = binding.recyclerViewGarage
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter

        homeViewModel.vehicles.observe(viewLifecycleOwner, Observer { vehicles ->
            adapter.updateVehicles(vehicles)
            progressBar.visibility = View.GONE
        })

        binding.searchView.queryHint = "Поиск по номеру автомобиля"
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filterByNumber(newText.orEmpty())
                return true
            }
        })

        val spreadsheetId = GoogleSheetsConfig.SPREADSHEET_ID
        val range = GoogleSheetsConfig.RANGE1
        val apiKey = GoogleSheetsConfig.API_KEY

        homeViewModel.fetchVehicleData(spreadsheetId, range, apiKey)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.activity_main_drawer, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val organization = when (menuItem.itemId) {
                    R.id.nav_home -> ""
                    R.id.nav_atrium -> "Атриум Сервис"
                    R.id.nav_bte -> "Бор Теплоэнерго"
                    R.id.nav_teplovik -> "Тепловик"
                    R.id.nav_kaldera -> "Кальдера"
                    R.id.nav_bef -> "БЭФ"
                    R.id.nav_neo -> "НЭО"
                    else -> return false
                }

                adapter.filterByOrganization(organization)
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}