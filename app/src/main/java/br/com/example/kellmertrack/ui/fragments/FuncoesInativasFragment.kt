package br.com.example.kellmertrack.ui.fragments

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.databinding.FuncoesInativasBinding
import br.com.example.kellmertrack.ui.MainActivity

class FuncoesInativasFragment: DialogFragment() {

    private lateinit var context : Context
    private lateinit var binding: FuncoesInativasBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.isCancelable = false
        return inflater.inflate(R.layout.funcoes_inativas, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FuncoesInativasBinding.bind(view)
        configuraBotao()
    }

    private fun configuraBotao(){
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val powerSaveStatus = powerManager.isPowerSaveMode

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val bluetoothStatus = bluetoothAdapter?.isEnabled == true

        binding.llLocation.visibility = if (!locationStatus) View.VISIBLE else View.GONE
        binding.llBluetooth.visibility = if (!bluetoothStatus) View.VISIBLE else View.GONE
        binding.llEnergy.visibility = if (powerSaveStatus) View.VISIBLE else View.GONE

        binding.btnOk.setOnClickListener {
            this.dismiss()
            (activity as MainActivity).checkDeviceFunctions()
        }
    }
}