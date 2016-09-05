package com.lyeeedar.Systems

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.utils.ImmutableArray
import com.lyeeedar.AI.Tasks.TaskMove
import com.lyeeedar.Components.*
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.Global
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 20-Mar-16.
 */

class TaskProcessorSystem(): EntitySystem(systemList.indexOf(TaskProcessorSystem::class))
{
	lateinit var entities: ImmutableArray<Entity>
	lateinit var sprites: ImmutableArray<Entity>
	lateinit var effects: ImmutableArray<Entity>
	val entitiesToBeProcessed: com.badlogic.gdx.utils.Array<Entity> = com.badlogic.gdx.utils.Array<Entity>(false, 32)

	override fun addedToEngine(engine: Engine?)
	{
		entities = engine?.getEntitiesFor(Family.all(TaskComponent::class.java, StatisticsComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
		sprites = engine?.getEntitiesFor(Family.all(SpriteComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
		effects = engine?.getEntitiesFor(Family.all(EffectComponent::class.java).get()) ?: throw RuntimeException("Engine is null!")
	}

	override fun update(deltaTime: Float)
	{
		var hasEffects = false
		for (entity in sprites)
		{
			val sprite = Mappers.sprite.get(entity)
			if (sprite.sprite.animation != null)
			{
				hasEffects = true;
				break
			}
		}

		if (!hasEffects)
		{
			hasEffects = effects.size() > 0
		}

		if (!hasEffects && entitiesToBeProcessed.size == 0)
		{
			// process player
			val player = Global.currentLevel.player

			processEntity(player)

			val playerTask = Mappers.task.get(player)
			if (playerTask.actionDelay < 0)
			{
				for (entity in entities)
				{
					if (entity != player)
					{
						val task = Mappers.task.get(entity)
						task.actionDelay -= playerTask.actionDelay

						if (task.actionDelay >= 0)
						{
							entitiesToBeProcessed.add(entity)
						}
					}
				}

				playerTask.actionDelay = 0f
			}
		}

		for (i in 0..entitiesToBeProcessed.size-1)
		{
			if (entitiesToBeProcessed.size > 0)
			{
				val entity = entitiesToBeProcessed[0]
				entitiesToBeProcessed.removeIndex(0)
				val finished = processEntity(entity)

				if (!finished) entitiesToBeProcessed.add(entity)
			}
		}
	}

	fun findClosestLeader(name: String, pos: Point): Entity?
	{
		var closest: Entity? = null
		var dist: Int = Int.MAX_VALUE

		for (entity in entities)
		{
			val tile = entity.tile() ?: continue
			val n = entity.name()

			if (n == name)
			{
				val d = tile.dist(pos)
				if (d < dist)
				{
					dist = d
					closest = entity
				}
			}
		}

		return closest
	}

	fun processEntityStats(e: Entity)
	{
		val stats = Mappers.stats.get(e)

		if (stats.hp == stats.maxHP)
		{
			stats.bonusHP = 0f
		}
		else if (stats.bonusHP > 0)
		{
			stats.hp += Math.max(stats.bonusHP, 10f)
		}

		if (!stats.staminaReduced && stats.stamina < stats.maxStamina)
		{
			stats.stamina += 10f
		}
	}

	fun processEntity(e: Entity): Boolean
	{
		val task = Mappers.task.get(e) ?: return true
		val stats = Mappers.stats.get(e) ?: return true
		val pos = Mappers.position.get(e) ?: return true

		if (stats.hp <= 0) return true
		if (pos.hasEffects()) return false

		stats.staminaReduced = false

		if (task.actionDelay >= 0)
		{
			if (task.tasks.size == 0)
			{
				if (task.leaderName != null && task.leader == null)
				{
					task.leader = findClosestLeader(task.leaderName!!, pos.position)
				}

				task.ai.setData("leader", task.leader)
				task.ai.update(e)
			}

			if (task.tasks.size > 0)
			{
				val t = task.tasks.removeIndex(0)

				if (t is TaskMove && e.pos().hasEffects(t.direction)) return false

				t.execute(e)
				task.actionDelay -= t.cost;

				e.pos().turnsOnTile++
				processEntityStats(e)

				e.postEvent(EventArgs(t.eventType, e, e, t.cost))
			}
			else
			{
				task.actionDelay = 0f
				return true
			}
		}

		return task.actionDelay < 0
	}
}
