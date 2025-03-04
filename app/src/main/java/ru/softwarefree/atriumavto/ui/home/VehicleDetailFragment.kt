package ru.softwarefree.atriumavto.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.softwarefree.atriumavto.databinding.FragmentVehicleDetailBinding
import ru.softwarefree.atriumavto.ui.data.Vehicle

class VehicleDetailFragment : Fragment() {

    private var _binding: FragmentVehicleDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVehicleDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val vehicle: Vehicle? = arguments?.getParcelable("vehicle")

        vehicle?.let {
            binding.apply {
                vehicleOrganization.text = "Организация - ${it.organization}"
                vehicleNumber.text = "Номер - ${it.number}"
                vehicleBrand.text = "Марка - ${it.brand}"
                vehicleDriver.text = "Водитель - ${it.driver}"
                vehicleVin.text = "VIN - ${it.vin}"
                vehicleYear.text = "Год выпуска - ${it.year}"
                vehicleMileage.text = "Пробег - ${it.mileage}"
                vehicleInsurance.text = "Страховка - ${it.insurance}"
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}