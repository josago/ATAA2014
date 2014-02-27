package edu.utexas.cs.tamerProject.featGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collections;
import java.util.Comparator;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

//import edu.utexas.cs.tamerProject.modeling.RegressionModel;

public class FeatGen_Mario extends FeatGenerator{
	
//	protected boolean verbose = true;
//	public Random randGenerator = new Random();
//	public static final Random staticRandGenerator = new Random();
//	public int[][] theObsIntRanges;
//	public double[][] theObsDoubleRanges; 
//	public int[][] theActIntRanges;
//	public double[][] theActDoubleRanges;
//	public static ArrayList<int[]> possActIntArrays;
//	////protected RegressionModel model;
//	protected int numFeatures;
//	
//	private RegressionModel modelForSupplFeatGen;
//	private FeatGenerator featGenForSupplModel;
//	private String featSource = "state-action";
	
	protected final int LOCAL_AREA = 4;
	protected final int NUM_ITEMS_AFTER_FILTER = 2;
	static boolean AT_END = false;
	
	private static final int NUM_ACTION_FEATS = 3;
	private static final int NUM_FEATS_PER_STATE_ITEM = 5;
	
	public FeatGen_Mario(int[][] theObsIntRanges, double[][] theObsDoubleRanges, 
			int[][] theActIntRanges, double[][] theActDoubleRanges){
		super(theObsIntRanges, theObsDoubleRanges, theActIntRanges, theActDoubleRanges);
		
		// remove actions that involve a fast speed but no movement
		int[] redundantActIntArray = {0, 0, 1}; // no jump
//		FeatGenerator.possStaticActions.remove(FeatGenerator.getActIntIndex(redundantActIntArray, FeatGenerator.possStaticActions));
//		redundantActIntArray[1] = 1; // jumping
//		FeatGenerator.possStaticActions.remove(FeatGenerator.getActIntIndex(redundantActIntArray, FeatGenerator.possStaticActions));
//	
		this.numFeatures = NUM_ACTION_FEATS + (NUM_ITEMS_AFTER_FILTER * NUM_FEATS_PER_STATE_ITEM);
	}
	
	public int[] getActionFeatIndices(){
		int[] actionFeatIndices = {0,1,2};
		return actionFeatIndices;
	}
	
	public int[] getNumFeatValsPerFeatI(){
		int[] numFeatValsPerFeatI = new int[NUM_ACTION_FEATS + 
		                                    (NUM_ITEMS_AFTER_FILTER * NUM_FEATS_PER_STATE_ITEM)];
		numFeatValsPerFeatI[0] = 3;
		numFeatValsPerFeatI[1] = 2;
		numFeatValsPerFeatI[2] = 2;
		return numFeatValsPerFeatI;
	}
	
	public double[] getSAFeats(Observation obs, Action act){
		//return getSAFeats(obs.intArray, obs.doubleArray, act.intArray, act.doubleArray);
		return getSAFeats(obs.intArray, obs.doubleArray, obs.charArray, act.intArray, act.doubleArray);
	}
	public double[] getSAFeats(int[] intStateVars, double[] doubleStateVars, char[] charStateVars, 
								int[] intActVars, double[] doubleActVars) {
		FilteredState filtState = getFilteredState(intStateVars, doubleStateVars, charStateVars, intActVars, doubleActVars);
		return fStateToSAFeats(filtState);
	}
	
	private double[] fStateToSAFeats(FilteredState filtState) {
		//System.out.println("FilteredState: " + filtState);
				
		ActionFs actionFs = filtState.actionFs;
		Vector<StateFs> stateFs = filtState.stateFs;
		double[] sAFeats = new double[this.numFeatures];
		sAFeats[0] = actionFs.marioMove - 1; // -1 b/c env is -1,0,1 and features are 0,1,2
		sAFeats[1] = actionFs.marioJump ? 1 : 0;
		sAFeats[2] = actionFs.marioSpeed ? 1 : 0;
		for (int i = 0; i < stateFs.size(); i++) {
			StateFs thisStateItem = stateFs.get(i);
			sAFeats[NUM_ACTION_FEATS + (i * NUM_FEATS_PER_STATE_ITEM)] = thisStateItem.dist;
			sAFeats[NUM_ACTION_FEATS + (i * NUM_FEATS_PER_STATE_ITEM) + 1] = thisStateItem.monster ? 1 : 0;//thisStateItem.monsterType;
			sAFeats[NUM_ACTION_FEATS + (i * NUM_FEATS_PER_STATE_ITEM) + 2] = thisStateItem.offX;
			sAFeats[NUM_ACTION_FEATS + (i * NUM_FEATS_PER_STATE_ITEM) + 3] = thisStateItem.offY;
			sAFeats[NUM_ACTION_FEATS + (i * NUM_FEATS_PER_STATE_ITEM) + 4] = thisStateItem.isPit ? 1 : 0;
		}
			
		//System.out.println("SAFeats: " + Arrays.toString(sAFeats));
		return sAFeats;
	}
	
	/**
	 * Returns the char representing the tile at the given location.
	 * If unknown, returns '\0'.
	 * 
	 * Valid tiles:
	 * M - the tile mario is currently on. there is no tile for a monster.
	 * $ - a coin
	 * b - a smashable brick
	 * ? - a question block
	 * | - a pipe. gets its own tile because often there are pirahna plants
	 *     in them
	 * ! - the finish line
	 * And an integer in [1,7] is a 3 bit binary flag
	 *  first bit is "cannot go through this tile from above"
	 *  second bit is "cannot go through this tile from below"
	 *  third bit is "cannot go through this tile from either side"
	 * 
	 * @param x
	 * @param y
	 * @param obs
	 * @return
	 */
	public static char getTileAt(double xf, double yf, Observation obs) {
		int x = (int)xf;
		if (x < 0)
			return '7';
		int y = 16 - (int)yf;
		x -= obs.intArray[0];
		if (x < 0 || x > 21 || y < 0 || y > 15)
			return '\0';
		int index = (y * 22) + x;
		return obs.charArray[index];
	}
	
	/**
	 * All you need to know about a monster.
	 * @author jasmuth
	 */
	static class Monster {
		double x;
		double y; 
		double sx; // The instantaneous change in x per step
		double sy; // The instantaneous change in y per step
		/**
		 * The monster type
		 * 0 - Little Mario
		 * 1 - Red Koopa
		 * 2 - Green Koopa
		 * 3 - Goomba
		 * 4 - Spikey
		 * 5 - Pirahna Plant
		 * 6 - Mushroom
		 * 7 - Fire Flower
		 * 8 - Fireball
		 * 10 - Big Mario
		 */
		int type; 
		String typeName; // A human recognizable title for the monster 
		boolean winged; // Winged monsters bounce up and down
	}
	
	/**
	 * Gets all the monsters from the observation. Mario is included in this list.
	 * 
	 * @param obs
	 * @return
	 */
	public static Monster[] getMonsters(Observation obs) {
		Vector<Monster> monster_vec = new Vector<Monster>();
		for (int i=0; 1+2*i<obs.intArray.length; i++) {
			Monster m = new Monster();
			m.type = obs.intArray[1+2*i];
			m.winged = obs.intArray[2+2*i]!=0;
			switch (m.type) {
			case 0:
			m.typeName = "Mario";
			break;
			case 1:
			m.typeName = "Red Koopa";
			break;
			case 2:
			m.typeName = "Green Koopa";
			break;
			case 3:
			m.typeName = "Goomba";
			break;
			case 4:
			m.typeName = "Spikey";
			break;
			case 5:
			m.typeName = "Piranha Plant";
			break;
			case 6:
			m.typeName = "Mushroom";
			break;
			case 7:
			m.typeName = "Fire Flower";
			break;
			case 8:
			m.typeName = "Fireball";
			break;
			case 10:
			m.typeName = "Big Mario";
			break;
			case 11:
			m.typeName = "Fire Mario";
			break;
			}
			m.x = obs.doubleArray[4*i];
			m.y = obs.doubleArray[4*i+1];
			m.sx = obs.doubleArray[4*i+2];
			m.sy = obs.doubleArray[4*i+3];
			monster_vec.add(m);
		}
		return monster_vec.toArray(new Monster[0]);
	}

	/**
	 * Gets just mario's information.
	 * 
	 * @param obs
	 * @return
	 */
	public static Monster getMario(Observation obs) {
		Monster[] monsters = getMonsters(obs);
		for (Monster m : monsters) {
			if (isMarioType(m.type))
				return m;
		}
		
		System.out.println("No Mario found");
		for (Monster m : monsters) {
			System.out.println("Monster type " + m.type + " at " + m.x + "," + m.y);
		}
		
		return null;
	}

	public static boolean isMarioType(int type) {
		return (type == 0 || type == 10 || type == 11);
	}
	
	
	
	/**
	The salience is hand determined here, but can be learned
	Sets the action FS
	Sets the state FS
	
	As calculated based on the parameterization at the time I'm writing this, a finished state has 4 main items: action features and 3 most
	"salient" state features.
	
	**/
	
	protected FilteredState getFilteredState(int[] intStateVars, double[] doubleStateVars, char[] charStateVars, int[] intActVars, double[] doubleActVars){
		Observation o = new Observation();
		o.intArray = intStateVars;
		o.doubleArray = doubleStateVars;
		o.charArray = charStateVars;
		
		Monster mario = FeatGen_Mario.getMario(o);
		Monster[] monsters = FeatGen_Mario.getMonsters(o);
		if (mario == null) {
			System.out.println(Arrays.toString(o.charArray));
		}
		
		/*Sets the action state for the fs*/
		////Action a = null;
		FilteredState fs = new FilteredState();

		////int idx = this_actions.size() - 1; // this selects the most recent action
		////if( this_actions.size() > 0 && (a = this_actions.get(idx)) != null ) {

		//// SET ACTION FEATURES 
		fs.setActionFs(intActVars[0] + 1,
						(intActVars[1] > 0) ? true : false,
						(intActVars[2] > 0) ? true : false);
		////}


		
		/*Immediate tiles for test*/
		char immedTiles[][] = new char[2 * LOCAL_AREA][2 * LOCAL_AREA];
		for(int i =- LOCAL_AREA;i < LOCAL_AREA; i++) {
			for(int j =- LOCAL_AREA;j < LOCAL_AREA; j++){
				int tileX = (int)mario.x + i;
				int tileY = (int)mario.y + j;
				immedTiles[i + LOCAL_AREA][j + LOCAL_AREA] = FeatGen_Mario.getTileAt(tileX, tileY, o);

			/*System.out.println("tile added x:" + tileX + ", y:" + tileY + ", val:" + immedTiles[i+LOCAL_AREA][j+LOCAL_AREA]);*/
			}
		}
			
		
		//// GET MOST IMPORTANT STATE FEATURES
		fs.setSalientFeatures(o, mario, immedTiles, monsters); 		// setSalientFeatures should be called AFTER setActionFs!
		
		return fs;
	}
		
	
	
	

	
	/**
	The goal of filtering state is to extract small state with salient features emphasized
	**/
	private class FilteredState{
		public ActionFs actionFs = null;
		public Vector<StateFs> stateFs = null;

		public String getHash(){
			String aHash=null,sHash=null;

			if( this.actionFs!=null)
			aHash = this.actionFs.getHash();
			
			/*if(this.stateFs!=null){
			sHash = this.stateFs.getHash();
			}*/
			// Concat the hash codes from each statefs
			if(this.stateFs!=null) {
				for(StateFs sfs:this.stateFs)
					sHash += sfs.getHash();
			}
			
			//String val = aHash+":"+sHash;
			String val = aHash+":"+sHash;
			return val;
		}

		public void setSalientFeatures(Observation o, 
						   Monster mario, 
						   char tiles[][], 
						   Monster monsters[]) {
			
			/*Extract all features*/
			Vector<StateFs> salFs = new Vector<StateFs>();
			for(int i=-LOCAL_AREA;i<LOCAL_AREA;i++) {
				for(int j=-LOCAL_AREA;j<LOCAL_AREA;j++) {
					int tileX = (int)mario.x;
					int tileY = (int)mario.y;
					
					//System.out.println("Tile add, x:" + tileX + ",y:" + tileY + ",val:" + tiles[idxX][idxY]);
					salFs.add(new StateFs(tiles[i+LOCAL_AREA][j+LOCAL_AREA], o, i, j, tileX, tileY));
				}
			}

			for(Monster m : monsters){
				if( !isMarioType(m.type) ){
					/*System.out.println("Monster added, m.type: " + m.type + ",offX:" + (m.x - mario.x) + ",offY:" + (m.y - mario.y));*/
					salFs.add(new StateFs(o, m, (int)(m.x-mario.x), (int)(m.y-mario.y)));
				}
			}

			// Rank
			Collections.sort(salFs, new DistanceRanker());
			Collections.sort(salFs, new BlockRanker());
			Collections.sort(salFs, new MonsterRanker());
			Collections.sort(salFs, new PitRanker());
			//Collections.sort(salFs, new SalienceRanker());

			// Take the top of the list
			if(salFs.size() > 0){
			
				// The first filtered state in the list
				//this.stateFs = salFs.get(0);
		
				this.stateFs = new Vector<StateFs>();
				for(int i = 0; i < NUM_ITEMS_AFTER_FILTER && i < salFs.size(); i++){
		
					StateFs fs = salFs.get(i);
					this.stateFs.add(fs);
					
					
//					if( fs.isPit )
//					System.out.println("Pit seen at x:" + fs.offX + 
//							   ",y:" + fs.offY + ", val:" + fs.tile + ", hash:" + fs.getHash());
//					else
//					if( !fs.monster )
//						System.out.println("Something else found seen at x:" + fs.offX + 
//								   ",y:" + fs.offY + ", val:" + fs.tile + ", hash:" + fs.getHash());
//					else
//						System.out.println("Monster seen at x:" + fs.offX + 
//						",y:" + fs.offY + ", val:" + fs.smushable + ", hash:" + fs.getHash());
					
				}
				//System.out.println("***"); 
				
				// Keeps the state space small
				Collections.sort(this.stateFs, new AlphabeticRanker());
		
				checkIfAtFinish(this.stateFs);
			}

			
		}
		
		/*Puts closer items lower in the list*/
		private class DistanceRanker implements Comparator<Object>{
			public int compare(Object obj1, Object obj2) {
				StateFs s1 = (StateFs)obj1;
				StateFs s2 = (StateFs)obj2;
				double rand = Math.random();
				if (s1.dist > s2.dist)
					return 1;
				else if (s1.dist < s2.dist)
					return -1;
				return 0;
			}
		}

		/*Puts coins($),smashable blocks (b), and question blocks(?) closer*/ 
		private class BlockRanker implements Comparator<Object>{
			public int compare(Object obj1, Object obj2) {
				StateFs s1 = (StateFs)obj1;
				StateFs s2 = (StateFs)obj2;
				boolean s1B = false;
				boolean s2B = false;
				
				if( s1.monster == false && (s1.tile=='b'|s1.tile=='$'|s1.tile=='?') )
					s1B=true;
				if( s2.monster == false && (s2.tile=='b'|s2.tile=='$'|s2.tile=='?') )
					s2B=true;
				
				if( s1B && !s2B )
					return -1;
				if( !s1B && s2B )
					return 1;
				return 0;
			}
		}

		/*Puts monsters lower in the list*/
		private class MonsterRanker implements Comparator<Object>{
			public int compare(Object obj1, Object obj2) {
				StateFs s1 = (StateFs)obj1;
				StateFs s2 = (StateFs)obj2;

				double rand = Math.random();				
				if( s1.monster == true && s2.monster == false )
					return -1;
				else if( s1.monster == false && s2.monster == true )
					return 1;
				return 0;
			}
		}

		/*Puts pits lower in the list*/
		private class PitRanker implements Comparator<Object>{

			public int compare(Object obj1, Object obj2) {
				StateFs s1 = (StateFs)obj1;
				StateFs s2 = (StateFs)obj2;
	
				if (s1.isPit)
					return -1;
				if (s2.isPit)
					return 1;
				return 0;
				}
		}

		private class AlphabeticRanker implements Comparator<Object>{

			public int compare(Object obj1, Object obj2) {
				StateFs s1 = (StateFs)obj1;
				StateFs s2 = (StateFs)obj2;
				return s1.getHash().compareTo(s2.getHash());
			}
		}
		

		public void setActionFs(int move,boolean jump,boolean speed){
			this.actionFs = new ActionFs();
			this.actionFs.marioMove = move;
			this.actionFs.marioJump = jump;
			this.actionFs.marioSpeed = speed;
		}

	}

	/*These are unconditionally set*/
	public class ActionFs {
		public boolean marioJump = false;//bit 1
		public boolean marioSpeed = false;//bit 2
		public int marioMove = 0;//0 left,1 none,2 right. bits 3-4

		public String getHash(){
			String val = "" + this.marioJump + ":" + this.marioSpeed + ":" + this.marioMove;
			return (val == null) ? "null" : val;
		}
	}
	
	
	/**
	   StateFs holds screen state
	**/
	public class StateFs {
		public int dist = 0; // 0 step, 1 jump, 2 beyond
		public boolean monster = false;
		public int monsterType = 0;
		public char tile = 0;//See monster and tile types
		public int smushable = 0; // 0 or 1, not smushable or smushable
	
		//Relative positions
		public int offX = 0;
		public int offY = 0;
	
		//Absolute positions
		public int tileX = 0;
		public int tileY = 0;
		
		public boolean isPit = false;
		public boolean atWallRight = false;
		
		public String toString() {
			String s = "Dist: " + dist;
			s += "; Monster? " + monster;
			s += "; Tile type: " + tile;
			s += "; Smushable? " + smushable;
			s += "; X dist: " + offX;
			s += "; Y dist: " + offY;
			s += "; Pit? " + isPit;
			s += "; WallToRight? " + atWallRight;
			return s;
		}
	
		/* Constructor takes a tile and the offsets relative to mario*/
		public StateFs(char tile, Observation o, int offX, int offY, int tileX, int tileY){
			this.dist=this.calcDistance(offX,offY);
			this.monster=false;
			this.monsterType = 0;
			this.tile=tile;
			
			// Absolute positions
			this.tileX=tileX;
			this.tileY=tileY;
			
			// Relative positions
			this.offX = offX;
			this.offY = offY;
	
			this.isPit = isPit(offX,offY,o);
			this.atWallRight = atWallRight(o);
//			System.out.println("Big constructor: " + this.offX+":"+this.offY+":"+this.monster+":"+this.tile+":"+this.smushable+":"+this.isPit+":"+atWallRight);
		}
			
		/* Constructor takes a monster and the offsets relative to mario*/
		public StateFs(Observation o, Monster m, int offX, int offY){
			this.dist = this.calcDistance(offX,offY);
			this.monster = true;
			this.monsterType = m.type;
			this.tile = '\0';
			this.smushable= (m.type==8|m.type==4|m.type==3) ? 0 : 1;
	
			this.offX = offX;
			this.offY = offY;
	
			this.tileX = (int)m.x;
			this.tileY = (int)m.y;
	
			this.isPit = false;
			this.atWallRight = atWallRight(o);
//			System.out.println("Small constructor: " + this.offX+":"+this.offY+":"+this.monster+":"+this.tile+":"+this.smushable+":"+this.isPit+":"+atWallRight);
		}
			
		/*Returns int for close, near, or far*/
		private int calcDistance(int offX,int offY){
			double edist = Math.sqrt(Math.pow(offX , 2) + Math.pow(offY , 2));
			return (int)edist;
		}
	
		public String getHash(){
			//	    String val= "" + this.offX+":"+this.offY+":"+this.monster+":"+this.tile+":"+this.smushable+":"+this.isPit+":"+this.atWallRight;
			String val= "" + this.offX+":"+this.offY+":"+this.monster+":"+this.tile+":"+this.smushable+":"+this.isPit+":"+atWallRight;
			return (val==null)?"null":val;
		}
	}

	
	
	private boolean isPit(int offX, int offY, Observation o){
		Monster mario = FeatGen_Mario.getMario(o);
		int x = (int)mario.x;
		int y = (int)mario.y;
	
		final int pitw = 4;
		final int pith = 1;
	
		boolean leftWall = false;
		boolean rightWall = false;
		boolean gap = false;
	
		// gapDX is the start of the gap in x
		int gapDX = 0;
		int gapEndDX = 0;
	 
		// The starting tile must be solid
		if( FeatGen_Mario.getTileAt(x+offX,y+offY,o) != '7' )
			return false;
	
		// The tile above the starting tile must be solid
		if( FeatGen_Mario.getTileAt(x+offX,y+offY+1,o) != ' ' &&
			FeatGen_Mario.getTileAt(x+offX,y+offY+1,o) != 'M')
			return false;
	
		for(int dx=x+offX;dx<=x+offX+pitw&&offX+pitw<11;dx++){
			boolean col = true;
			for(int dy=y+offY;dy>=0;dy--){
		 
				char tile = FeatGen_Mario.getTileAt(dx,dy,o);	
				
				if( leftWall == false ){
					if( tile == ' ' && col == true ){
						leftWall = true;
						col = false;
						gapDX=dx;
					}
					else if( tile == ' ' && col == false )
						return false;
				}
				else if( gap == false ){
					if( tile == '7' && col == true ){
						gap = true;
						col = false;
						gapEndDX = dx;
					}
					else if( tile == '7' && col == false )
						return false;
				}
			}
		}
	
		//System.out.println("Found a pit at offX:"+offX+",offY:"+offY+",x:" + (x+offX) + ",y:"+(y+offY)+",leftWall: " + leftWall + ", gap: " + gap);
		
		// This might be a gap, but we want to be sure
		if( leftWall && gap && y+offY >= pith ){
			
			//System.out.println("Checking gap  at gapDX:"+gapDX+",gapEndDX:"+gapEndDX+",x:" + (x+offX) + ",y:"+(y+offY)+",height:"+(y+offY));
	
			// Make sure the gap extends to 0 and is empty
			for(int dx=gapDX;dx<gapEndDX;dx++){
				for(int dy=y+offY;dy>=0;dy--){
					char tile = FeatGen_Mario.getTileAt(dx,dy,o);
					if( tile != ' ' && tile != '\0' && tile !='M' ){
						//System.out.println("Checking gap area failed on tile:" + tile);
						return false;
					}
				}
			}
	
			// Check the tiles immediately above the top of the gap
			for(int dx=gapDX;dx<gapEndDX;dx++){
				char tile = FeatGen_Mario.getTileAt(dx,y+offY+1,o);
				//System.out.println("Testing tile:" + tile);
				if( tile != ' ' && tile != '\0' && tile !='M' )
				{
					//System.out.println("Checking tiles above gap failed on tile:" + tile);
					return false;
				}
			}
	
			// We made it through the gap-check
			return true;
		}
		else
			return false;
	}
	
	private boolean atWallRight(Observation o){
		Monster mario = FeatGen_Mario.getMario(o);
		double x = mario.x;
		double y = mario.y;
		if( FeatGen_Mario.getTileAt(x+1, y+1, o) == '7' ||
			FeatGen_Mario.getTileAt(x+1, y+1, o) == '|' ){
			
			//System.out.println("At WALL RIGHT!");
			return true;
		}
		return false;
	}	

	protected static void checkIfAtFinish(Vector<StateFs> stateFs){
		for(StateFs sfs:stateFs){
			if( sfs.tile == '!' && sfs.offX <= 4 )
				{AT_END = true; return;}
		}
		AT_END = false;
	}
	
	
	public double[] getSAFeats(int[] intStateVars, double[] doubleStateVars,
			int[] intActVars, double[] doubleActVars) {
		System.err.println("This getSAFeats without o.charArray should not be used for Mario." +
				" Exiting in "+ this.getClass() + ".");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
	
	public double[] getSSFeats(int[] intStateVars, double[] doubleStateVars, int[] intNextStateVars, double[] doubleNextStateVars){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
	public double[] getSFeats(Observation obs){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
	
	
	
	// These currently do not support feature generators with a supplemental model added.
	// Such support is unnecessary as long as this is only used by HInfluence.
	public double[] getMaxPossFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
	public double[] getMinPossFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	
	}
	public double[] getMaxPossSFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
	public double[] getMinPossSFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		int[] a = {}; int b = a[0];
		System.exit(1);
		return new double[0];
	}
}


	



