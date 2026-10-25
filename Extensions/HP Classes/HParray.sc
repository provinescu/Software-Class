+ Collection {

	// methode pour forcer combinaison de MultiArray en 1 seule Array pour MultiAgents

	soloArray {arg newArray;

		this.do({arg item, rang;

			if(item.isString,

				{newArray=newArray.add(item)},

				{if(item.size > 0,

					{newArray = item.soloArray(newArray)},

					{newArray=newArray.add(item)});
			});

		});

		^newArray;

	}

}