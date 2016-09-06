package com.lyeeedar.Screens

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.lyeeedar.Combo.ComboStep
import com.lyeeedar.Combo.SlashComboStep
import com.lyeeedar.Combo.WaitComboStep
import com.lyeeedar.Components.*
import com.lyeeedar.Global
import com.lyeeedar.Level.Level
import com.lyeeedar.Level.Tile
import com.lyeeedar.Renderables.Sprite.DirectionalSprite
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Systems.createEngine
import com.lyeeedar.Util.*

class TestDirectionalScreen : AbstractScreen()
{
	override fun create()
	{
		Global.currentLevel = Level()
		Global.currentLevel.ambient.set(Colour.WHITE)

		Global.currentLevel.grid = Array2D(10, 10) { x, y -> Tile() }
		for (tile in Global.currentLevel.grid)
		{
			val e = Entity()
			val sprite = SpriteComponent(AssetManager.loadSprite("Oryx/uf_split/uf_terrain/ground_grass_1"))
			e.add(sprite)

			val pos = PositionComponent()
			e.add(pos)
			pos.position = tile
			pos.slot = SpaceSlot.FLOOR

			tile.contents[SpaceSlot.FLOOR] = e

			Global.engine.addEntity(e)
		}

		val player = EntityLoader.load("player")

		Global.currentLevel.player = player
		Global.currentLevel.grid[2, 2].contents[player.pos()!!.slot] = player
		player.pos()!!.position = Global.currentLevel.grid[2, 2]

		Global.engine.addEntity(player)

		val monster = EntityLoader.load("monster")

		val dirSprite = DirectionalSprite()
		dirSprite.upSprites["idle"] = AssetManager.loadSprite("Monster/rat_up_idle", drawActualSize = true)
		dirSprite.downSprites["idle"] = AssetManager.loadSprite("Monster/rat_down_idle", drawActualSize = true)
		dirSprite.upSprites["attack"] = AssetManager.loadSprite("Monster/rat_up_attack", drawActualSize = true)
		dirSprite.downSprites["attack"] = AssetManager.loadSprite("Monster/rat_down_attack", drawActualSize = true)

		monster.add(DirectionalSpriteComponent(dirSprite))

		Global.currentLevel.grid[7, 7].contents[monster.pos()!!.slot] = monster
		monster.pos()!!.position = Global.currentLevel.grid[7, 7]

		val combo = ComboComponent()
		monster.add(combo)
		val step = WaitComboStep()
		step.nextSteps.add(SlashComboStep())
		combo.combos.add(step)

		Global.engine.addEntity(monster)
	}

	override fun doRender(delta: Float)
	{
		Global.engine.update(delta)
	}
}