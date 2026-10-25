HPclassNeurones {

	classvar kk;

	var neucouche, neuin1, neuin2, neucouche1, neucouche2, neuout1, neuout2, poids, seuils, datasIn, datasOut;

	*new {arg i=1, c=5, o=1;

		kk=exp(1);

		^super.new.init(i, c, o);

	}

	init {arg i,c,o;

		var x;

		i = i.clip(1, 64);
		c = c.clip(1, 64);
		o = o.clip(1, 64);

		neucouche = c;
		neuin1 = 1;
		neuin2 = i;
		neucouche1 = i + 1;
		neucouche2 = neuin2 + neucouche;
		neuout1 = neucouche2 + 1;
		neuout2 = neuout1 + (o - 1);

		// init poids
		poids=[];

		for(1, neuout2,
			{
				x=[];

				for(1, neuout2,
					{
						x = x.add(1.0.rand2);
				});

				poids = poids.add(x);

		});

		// init seuils
		seuils=[];

		for(1, neuout2,

			{
				seuils = seuils.add(0.5.rand2);
		});
	}

	load {arg p, s;

		if(p != nil,
			{
				poids=p;
		});
		if(s != nil,
			{
				seuils=s;
		});
	}

	save {

		^[poids, seuils]

	}

	// Reseau sortie [0,1]
	next {arg datasIn, datasOut, mode=1, apprentissage=0.5, temperature=1.0;

		var neuactiva=[], neuerreur=[], activationneurones=0, activationeurreurs=0, sortie=[];

		// Init dataIn and dataOut
		if(datasOut == nil,
			{
				datasOut = datasIn;
		});

		//Init activation and error
		for(1, neuout2,
			{
				neuactiva = neuactiva.add(0);
				neuerreur = neuerreur.add(0);
		});

		for(neucouche1, neucouche2,
			{arg j;
				activationneurones=0;
				for(1, neuin2,
					{arg i;
						activationneurones = activationneurones + (datasIn.at(i-1) * poids.at(i-1).at(j-1));
				});
				neuactiva.put(j - 1, 1 / (1 + (kk**(((-1.0) * (activationneurones - seuils.at(j-1))) / temperature))));
		});
		for(neuout1, neuout2,
			{arg j;
				activationneurones=0;
				for(neucouche1, neucouche2,
					{arg i;
						activationneurones = activationneurones + (neuactiva.at(i-1) * poids.at(i-1).at(j-1));
				});
				neuactiva.put(j - 1, 1 / (1 + (kk**(((-1.0) * (activationneurones - seuils.at(j-1))) / temperature))));
		});

		if(mode == 1,
			{
				for(neuout1, neuout2,
					{arg i;
						neuerreur.put(i - 1,(datasOut.at(i - 1 - neuout1 + 1) - neuactiva.at(i-1)) * neuactiva.at(i-1) * (1 - neuactiva.at(i-1)));
						seuils.put(i - 1,seuils.at(i-1) - (neuerreur.at(i-1) * apprentissage));
				});
				for(neucouche1, neucouche2,
					{arg i;
						activationeurreurs=0;
						for(neuout1, neuout2,
							{arg j;
								activationeurreurs = activationeurreurs + (neuerreur.at(j-1) * poids.at(i-1).at(j-1));
						});
						neuerreur.put(i - 1, activationeurreurs * neuactiva.at(i-1) * (1 - neuactiva.at(i-1)));
				});
				for(1, neuin2,
					{arg i;
						activationeurreurs=0;
						for(neucouche1, neucouche2,
							{arg j;
								activationeurreurs = activationeurreurs + (neuerreur.at(j-1) * poids.at(i-1).at(j-1));
						});
						neuerreur.put(i - 1, activationeurreurs * neuactiva.at(i-1) * (1 - neuactiva.at(i-1)));
				});
				for(neucouche1, neucouche2,
					{arg i;
						seuils.put(i-1, seuils.at(i-1) - (neuerreur.at(i-1) * apprentissage));
						for(neuout1, neuout2,
							{arg j;
								poids.at(i-1).put(j-1, poids.at(i-1).at(j-1) - ((-1.0) * apprentissage * neuerreur.at(j-1) * neuactiva.at(i-1)));
						});
				});
				for(1, neuin2,
					{arg i;
						seuils.put(i-1, seuils.at(i-1) - (neuerreur.at(i-1) * apprentissage));
						for(neucouche1, neucouche2,
							{arg j;
								poids.at(i-1).put(j-1, poids.at(i-1).at(j-1) - ((-1.0) * apprentissage * neuerreur.at(j-1) * neuactiva.at(i-1)));
						});
				});
		});

		// Sortie  Array neural network
		for(neuout1, neuout2,
			{arg i;
				sortie = sortie.add(neuactiva.at(i-1));
		});

		^sortie;
	}

}