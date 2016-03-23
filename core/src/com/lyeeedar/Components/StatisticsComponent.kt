package com.lyeeedar.Components

import com.badlogic.ashley.core.Component
import com.lyeeedar.Enums
import com.lyeeedar.Util.FastEnumMap
import java.util.*

/**
 * Created by Philip on 21-Mar-16.
 */

class StatisticsComponent: Component
{
	constructor()

	var name: String = ""
	val stats: FastEnumMap<Enums.Statistic, Float> = Enums.Statistic.getStatisticsBlock(0f)
	val variableMap: HashMap<String, Float> = HashMap()
	val factions: HashSet<String> = HashSet()
	var hp: Float = 1f
	var stamina: Float = 1f
}