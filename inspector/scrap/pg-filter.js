
pg('>', 5)			 	=> test > 5

pg('like', 'happy')		=> test like 'happy'

pg('&', 7, '<>', 0)		=> (test & 7) <> 0

pg.within(geom)			=> ST_Within(test, geom)

pg.within_distance(geom, 5) => ST_DWithin(test, geom, 5)

pg(
	pg.distance(geom), '<', 50		=> ST_Distance(test, geom) < 50
)

pg(
	pg.distance(geom), '>', 50, '<>', false		=> (ST_Distance(test, geom) < 50) <> False
)

pg('ST_Distance(test,geom)', )