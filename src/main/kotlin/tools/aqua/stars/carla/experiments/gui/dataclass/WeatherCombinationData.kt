package tools.aqua.stars.carla.experiments.gui.dataclass
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherCombinationData(
    val value: List<WeatherCombinationPair>
)