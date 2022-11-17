package dk.easj.anbo.sensorlistcheckbox

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import dk.easj.anbo.sensorlistcheckbox.databinding.FragmentFirstBinding

// Kotlin adapted from https://github.com/andersbor/AndroidSensorExample
class FirstFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var sensorManager: SensorManager

    // Two parallel lists
    private lateinit var allSensors: List<Sensor>
    private val checkBoxList = mutableListOf<CheckBox>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        val layout = binding.mainLinearLayout
        for (sensor in allSensors) {
            val checkBox = CheckBox(requireContext())
            checkBoxList.add(checkBox)
            checkBox.text = sensor.name
            layout.addView(checkBox)
        }

        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                registerListeners()
            } else {
                unregisterListeners()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun registerListeners() {
        for (i in allSensors.indices) {
            if (checkBoxList[i].isChecked) {
                sensorManager.registerListener(
                    this, allSensors[i],
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
        }
    }

    private fun unregisterListeners() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorName = event?.sensor?.name
        binding.textviewSensorName.text = sensorName
        binding.textviewSensorData.text = event?.values?.joinToString(", ")
        if (sensorName != null && event != null) {
            UdpBroadcastHelper().sendUdpBroadcast(sensorName, event.values, 14593)
        }

        val message = "${event?.sensor?.name}\t ${event?.values?.joinToString(", ")}"
        Log.d("APPLE", message)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nothing
    }


}