package ru.softwarefree.atriumavto.ui.consumption

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.softwarefree.atriumavto.databinding.FragmentConsumptionDetailBinding
import ru.softwarefree.atriumavto.ui.data.Consumption

class ConsumptionDetailFragment : Fragment() {

    private var _binding: FragmentConsumptionDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsumptionDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val consumption: Consumption? = arguments?.getParcelable("consumption")

        consumption?.let {
            binding.apply {
                pointOfOrder.apply {
                    text =
                        it.pointOfOrder.takeIf { it.isNotEmpty() }?.let { "${it} - Приказа АМ23р" }
                            ?: ""
                    visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                }
                carBrand.apply {
                    text = it.carBrand.takeIf { it.isNotEmpty() }?.let { "Марка автомобиля - $it" }
                        ?: ""
                    visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                }
                numberOfCylinders.apply {
                    text = it.numberOfCylinders.takeIf { it.isNotEmpty() }
                        ?.let { "Число цилиндров - $it" } ?: ""
                    visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                }
                power.apply {
                    text = it.power.takeIf { it.isNotEmpty() }?.let { "Мощность двигателя - $it" }
                        ?: ""
                    visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                }
                engineCapacity.apply {
                    text = it.engineCapacity.takeIf { it.isNotEmpty() }
                        ?.let { "Объем двигателя - $it" } ?: ""
                    visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                }
                transmission.apply {
                    text =
                        it.transmission.takeIf { it.isNotEmpty() }?.let { "Коробка передач - $it" }
                            ?: ""
                    visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                }
                fuelConsumptionRate.apply {
                    text = it.fuelConsumptionRate.takeIf { it.isNotEmpty() }
                        ?.let { "Норма расхода - $it" } ?: ""
                    visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}