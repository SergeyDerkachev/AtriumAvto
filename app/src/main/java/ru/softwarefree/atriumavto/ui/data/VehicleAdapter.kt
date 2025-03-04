package ru.softwarefree.atriumavto.ui.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.softwarefree.atriumavto.databinding.ItemVehicleBinding

class VehicleAdapter(
    private var vehicles: List<Vehicle>,
    private val onClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    private var filteredVehicles: List<Vehicle> = vehicles

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val binding = ItemVehicleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = filteredVehicles[position]
        holder.bind(vehicle)
        holder.itemView.setOnClickListener {
            onClick(vehicle)
        }
    }

    override fun getItemCount(): Int = filteredVehicles.size

    fun updateVehicles(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        filteredVehicles = newVehicles
        notifyDataSetChanged()
    }

    fun filterByOrganization(organization: String) {
        filteredVehicles = if (organization.isEmpty()) {
            vehicles
        } else {
            vehicles.filter { it.organization.equals(organization, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun filterByNumber(query: String) {
        filteredVehicles = if (query.isEmpty()) {
            vehicles
        } else {
            vehicles.filter { it.number.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    class VehicleViewHolder(private val binding: ItemVehicleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vehicle: Vehicle) {
            binding.vehicleOrganization.text = "Организация - ${vehicle.organization}"
            binding.vehicleNumber.text = "Номер - ${vehicle.number}"
            binding.vehicleBrand.text = "Марка - ${vehicle.brand}"
            binding.vehicleDriver.text = "Водитель - ${vehicle.driver}"
        }
    }
}