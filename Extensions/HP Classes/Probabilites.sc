+ Collection {

	// Methode pour enlever un objet a une position donnee
	removeObjetAt {arg position;
		//var objet;
		//objet=this.at(position);
			^this.remove(this.at(position))
	}

	// Calcul de la m�diane

	mediane {| function |var liste, dim, position, mediane;
		dim=this.size;
		if(dim > 0,
		{liste=this.copy.sort;
		position=(dim+1/2).floor;
		if(dim.odd ,{mediane=liste.at(position-1)},
		{mediane=(liste.at(position-1)+liste.at(position))/2});
		^mediane},{^0}); // sortie mediane
	}

	// Calcul des quartiles
	quartiles {| function | var liste, listeq1, listeq3, dim, position, 	mediane, q1, q3, ecartq, ecartsemiq;
		liste=this.copy.sort;
		dim=this.size;
		if(dim < 2 , {if(dim == 0, {^[0, 0, 0, 0, 0]},
			{^[liste.at(0).value,liste.at(0).value,liste.at(0).value,0,0]})},
		{
		mediane=liste.mediane;
		position=dim/2;
		if(dim.odd,
		{
		position = position.floor.asInteger;
		listeq1=liste.copyRange(0,position-1);
		listeq3=liste.copyRange(position+1,dim);
		},
		{
		position = position.floor.asInteger;
		listeq1=liste.copyRange(0,position-1);
		listeq3=liste.copyRange(position,dim);
		});
		q1=listeq1.mediane;
		q3=listeq3.mediane;
		ecartq=q3-q1;
		ecartsemiq=ecartq/2;
		^[q1, mediane, q3, ecartq, ecartsemiq];
		}) // sortie des valeurs quartiles
	}

	// Calcul de la variance
	variance {| function | var liste, moyenne, variance, sum=0, dim;
		dim=this.size;
		if(dim > 0,
		{moyenne=this.mean;
		this.do({arg item; sum=sum+((item-moyenne)**2)});
		variance=sum/dim;
		^variance},{^0}); // sortie de la variance

	}

	// Calcul de l'ecart-type Individus
	ecartType {| function | var liste, moyenne, variance, sum=0, dim;
		dim=this.size;
		if(dim > 0,
		{moyenne=this.mean;
		this.do({arg item, i;sum=sum+((item-moyenne)**2)});
		variance=sum/dim;
		^variance.sqrt},{^0}); // sortie ecart-type = racine carree de la variance
	}

	// Calcul de l'ecart-type Populations
	ecartTypeP {| function | var liste, moyenne, variance, sum=0, dim;
		dim=this.size - 1;
		if(dim > 0,
		{moyenne=this.mean;
		this.do({arg item, i;sum=sum+((item-moyenne)**2)});
		variance=sum/dim;
		^variance.sqrt},{^0}); // sortie ecart-type = racine carree de la variance
	}

	// Calcul coefficient de dissymetrie
	dissymetrie	{| function | var moyenne, mediane, ecartType;
		if(this.size > 0,
		{moyenne=this.mean;
		mediane=this.mediane;
		ecartType=this.ecartTypeP;
		if(ecartType <= 0, {ecartType = 1});
		^3*(moyenne - mediane) / ecartType},
			{^0}
		)
	}
}
