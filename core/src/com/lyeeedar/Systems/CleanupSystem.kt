package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.lyeeedar.Components.Mappers
import com.lyeeedar.Components.StatisticsComponent
import com.lyeeedar.Level.Tile

/**
 * Created by Philip on 21-Mar-16.
 */

class CleanupSystem(): IteratingSystem(Family.all(StatisticsComponent::class.java).get(), 20)
{
	lateinit var eng: Engine


	override fun addedToEngine(engine: Engine?)
	{
		this.eng = engine ?: return

		super.addedToEngine(engine)
	}

	override fun processEntity(entity: Entity?, deltaTime: Float)
	{
		val stats = Mappers.stats.get(entity)

		if (stats.hp < 0)
		{
			// only cleanup if tile has no effects
			val sprite = Mappers.sprite.get(entity)
			if (sprite?.sprite?.spriteAnimation != null) return

			val pos = Mappers.position.get(entity)
			if (pos?.position is Tile)
			{
				val tile = (pos.position as Tile)

				if (tile.effects.size != 0) return

				tile.contents.remove(pos.slot)
			}

			eng.removeEntity(entity)
		}
	}

}