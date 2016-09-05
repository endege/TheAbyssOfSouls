package com.lyeeedar.AI.Tasks

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.scenes.scene2d.actions.EventAction
import com.lyeeedar.Components.*
import com.lyeeedar.Direction
import com.lyeeedar.EquipmentSlot
import com.lyeeedar.Events.EventActionDamage
import com.lyeeedar.Events.EventActionGroup
import com.lyeeedar.Events.EventArgs
import com.lyeeedar.Global
import com.lyeeedar.Level.Item
import com.lyeeedar.Renderables.Animation.BumpAnimation
import com.lyeeedar.Renderables.Sprite.Sprite

/**
 * Created by Philip on 29-Mar-16.
 */

class TaskAttack(var direction: Direction): AbstractTask(EventComponent.EventType.ATTACK)
{
	override fun execute(e: Entity)
	{
		val tile = e.tile() ?: return
		e.sprite().sprite.animation = BumpAnimation.obtain().set(0.25f, direction)

		val next = tile.neighbours.get(direction)

		val weapon: Item = e.getEquip(EquipmentSlot.WEAPON)

		val sprite = weapon.hitSprite?.copy() ?: return

		val effect = Entity()
		effect.add(EffectComponent(sprite, direction, Sprite.AnimationStage.MIDDLE, EventArgs(EventComponent.EventType.HIT, null, effect, 0f)))

		val event = EventComponent()
		val group = EventActionGroup()
		group.actions.add(EventActionDamage(group, weapon.attack))
		event.registerHandler(EventComponent.EventType.HIT, group)
		effect.add(event)

		val pos = PositionComponent(next)
		pos.max = next
		effect.add(pos)
		next.effects.add(effect)

		Global.engine.addEntity(effect)
	}
}