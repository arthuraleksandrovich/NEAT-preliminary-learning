package mapmakers;

import galaxy.Coords;
import galaxy.Player;

class PlanetData {
	final Player owner;
	final int numUnits;
	final int radius;
	final int prodTime;
	final Coords coords;
	
	PlanetData(Player owner, int numUnits, int radius, int prodTime, Coords coords){
		this.owner=owner;
		this.numUnits=numUnits;
		this.radius=radius;
		this.prodTime=prodTime;
		this.coords=coords;
	}
}
