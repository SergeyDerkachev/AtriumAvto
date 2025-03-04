package ru.softwarefree.atriumavto.ui.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.softwarefree.atriumavto.databinding.ItemConsumptionBinding

class ConsumptionAdapter(
    private var consumption: List<Consumption>,
    private val onClick: (Consumption) -> Unit
) : RecyclerView.Adapter<ConsumptionAdapter.ConsumptionViewHolder>() {

    private var filteredConsumption: List<Consumption> = consumption

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsumptionViewHolder {
        val binding = ItemConsumptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConsumptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConsumptionViewHolder, position: Int) {
        val consumption = filteredConsumption[position]
        holder.bind(consumption)
        holder.itemView.setOnClickListener {
            onClick(consumption)
        }
    }

    override fun getItemCount(): Int = filteredConsumption.size

    fun updateConsumption(newConsumption: List<Consumption>) {
        consumption = newConsumption
        filteredConsumption = newConsumption
        notifyDataSetChanged()
    }

    fun filterByBrand(query: String) {
        filteredConsumption = if (query.isEmpty()) {
            consumption
        } else {
            consumption.filter { it.carBrand.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    class ConsumptionViewHolder(private val binding: ItemConsumptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(consumption: Consumption) {
            binding.pointOfOrder.text = "${consumption.pointOfOrder} - Приказа АМ23р"
            binding.carBrand.text = "Марка автомобиля - ${consumption.carBrand}"
            binding.fuelConsumptionRate.text = "Норма расхода - ${consumption.fuelConsumptionRate}"
        }
    }
}