// A Software by Herve Provini

HPgenomeEditorAllInOne {

	*new	{arg agent=0;

		^super.new.init(agent);

	}

	init	{arg agent;

		// Init GUI + edit genomes
		// utilise les variables ~genomes et ~agents de AgentsBand !!!!

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system

		~flagHPgenomeEditor = 'on';

		this.edit(agent);

	}

	edit {arg agent, listeView=[];

		//Activation genomes
		~wEditor = Window("Genome Editor for AgentsBand by HP", Rect(0, 0, 800, 800), scroll: true);
		~wEditor.view.decorator = FlowLayout(~wEditor.view.bounds);
		//~wEditor.acceptsMouseOver_(true);
		StaticText(~wEditor, Rect(0, 0, 500, 18)).string_("A Genome Editor for AgentsBand").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 14));
		~wEditor.view.decorator.nextLine;
		// menu Genome
		~menuGenomeAll = PopUpMenu(~wEditor,Rect(0, 0, 125, 18)).background_(Color.grey(0.5, 0.8)).items = ["Genome Menu", "Load Genomes", "Save Genomes"];
		~menuGenomeAll.action={arg item, file;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load Genomes
				1, {~userOperatingSystem.valueAction_(6);~menuGenomeAll.valueAction_(0)},
				// Save Genomes
				2, {~userOperatingSystem.valueAction_(12);~menuGenomeAll.valueAction_(0)}
			);
		};
		~menuGenomeAll.focus;
		// Evaluate Genome
		~evaluateGenomeAll = Button(~wEditor, Rect(10, 10, 250, 18)).states=[["Evaluation Genomes", Color.black, Color.green(0.8, 0.25)]];
		~evaluateGenomeAll.action={arg item;
			~fonctionRecordScore.value("~evaluateGenomeAll", 1);
			if(~agents != 0 ,{~fonctionUpdateGenomeAll.value});// Update Display
		};
		~validGenomeAll = Button(~wEditor,Rect(0, 0, 250, 18)).states=[["Validation Genomes", Color.black, Color.blue(0.8, 0.25)]];
		~validGenomeAll.action = {arg view;
			var genomeAll=[], genomeAgent=[];
			// Validation genomes
			~agents.do({arg agent;
				var envTime, envLevel, env=[];
				genomeAgent=[];
				listeView.do({arg view, item;
					if(view != nil, {
						// Cas Speciaux
						if(item == 4 or: {item == 5}, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 127)});
						if(item == 6, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 127 / 254)});
						if(item == 11, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 16 / 80)});
						if(item == 12, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 13, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / ~tempsmaxsignal)});
						if(item == 14, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~displaySons.size - 1).max(1))});
						if(item == 15, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 16, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 18, {env=view.string.interpret.wrapAt(agent);
							envTime = env.value.wrapAt(0);
							envLevel = env.value.wrapAt(1);
							forBy(18, 25, 1 , {arg i; genomeAgent=genomeAgent.add(envLevel.wrapAt(i-18))});
							forBy(26, 33, 1 , {arg i; genomeAgent=genomeAgent.add(envTime.wrapAt(i-26))});
						});// Special Envelopes
						if(item == 34, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~listSynth.size - 1))});
						if(item == 38, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / ~sourceOutAgents.size)});
						if(item == 39, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 16)});
						//if(item == 40, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent))});
						if(item == 41, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 42, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 256)});
						if(item == 43, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 12)});
						if(item == 45, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~listeAlgorithm.size - 1).max(1))});
						// Cas Normaux
						if(item != 4 and: {item != 5} and: {item != 6} and: {item != 11} and: {item != 12} and: {item != 13} and: {item != 14} and: {item != 15} and: {item != 16} and: {item != 18} and: {item != 34} and: {item != 38} and: {item != 39} and: {item != 41} and: {item != 42} and: {item != 43} and: {item != 45}, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent))});
					});
				});
				genomeAll=genomeAll.add(genomeAgent);
			});
			~genomes=genomeAll;
			~fonctionRecordScore.value("~validGenomeAll", ~genomes);
		};
		~startTdefGenomeAll = Button(~wEditor,Rect(10, 10, 100, 18)).states=[["Tdef Display Off", Color.black, Color.green(0.8, 0.25)],["Tdef Display On", Color.black, Color.red(0.8, 0.25)]];
		~startTdefGenomeAll.action = {arg view;
			if (view.value == 1, {// Start Tdef
				~routineGenomeAll.play(quant: Quant.new(1, 0.0, 0.0));
				//~startTdefGenomeAll2.valueAction_(1);
				//~startTdefGenomeAll3.valueAction_(1);
				//~startTdefGenomeAll4.valueAction_(1);
			},
			{// Stop Tdef
				~routineGenomeAll.stop;
				//~startTdefGenomeAll2.valueAction_(0);
				//~startTdefGenomeAll3.valueAction_(0);
				//~startTdefGenomeAll4.valueAction_(0);
			});
		};
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Ageing").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag0 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag0.hasVerticalScroller_(true);
		~ag0.hasHorizontalScroller_(true);
		~ag0.autohidesScrollers_(true);
		listeView=listeView.add(~ag0);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Moving").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag1 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag1.hasVerticalScroller_(true);
		~ag1.hasHorizontalScroller_(true);
		~ag1.autohidesScrollers_(true);
		listeView=listeView.add(~ag1);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Agent Vision").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag2 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag2.hasVerticalScroller_(true);
		~ag2.hasHorizontalScroller_(true);
		~ag2.autohidesScrollers_(true);
		listeView=listeView.add(~ag2);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Signal Vision").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag3 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag3.hasVerticalScroller_(true);
		~ag3.hasHorizontalScroller_(true);
		~ag3.autohidesScrollers_(true);
		listeView=listeView.add(~ag3);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Fhz Low").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag4 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag4.hasVerticalScroller_(true);
		~ag4.hasHorizontalScroller_(true);
		~ag4.autohidesScrollers_(true);
		listeView=listeView.add(~ag4);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Fhz Range").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag5 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag5.hasVerticalScroller_(true);
		~ag5.hasHorizontalScroller_(true);
		~ag5.autohidesScrollers_(true);
		listeView=listeView.add(~ag5);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Translate Fhz").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag6 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag6.hasVerticalScroller_(true);
		~ag6.hasHorizontalScroller_(true);
		~ag6.autohidesScrollers_(true);
		listeView=listeView.add(~ag6);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Amplitude Low").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag7 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag7.hasVerticalScroller_(true);
		~ag7.hasHorizontalScroller_(true);
		~ag7.autohidesScrollers_(true);
		listeView=listeView.add(~ag7);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Amplitude Range").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag8 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag8.hasVerticalScroller_(true);
		~ag8.hasHorizontalScroller_(true);
		~ag8.autohidesScrollers_(true);
		listeView=listeView.add(~ag8);
		~wEditor.view.decorator.nextLine;
		// menu Genome
		~menuGenomeAll2 = PopUpMenu(~wEditor,Rect(0, 0, 125, 18)).background_(Color.grey(0.5, 0.8)).items = ["Genome Menu", "Load Genomes", "Save Genomes"];
		~menuGenomeAll2.action={arg item, file;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load Genomes
				1, {~userOperatingSystem.valueAction_(6)},
				// Save Genomes
				2, {~userOperatingSystem.valueAction_(12)}
			);
			~menuGenomeAll.valueAction_(0);
		};
		// Evaluate Genome
		~evaluateGenomeAll2 = Button(~wEditor, Rect(10, 10, 250, 18)).states=[["Evaluation Genomes", Color.black, Color.green(0.8, 0.25)]];
		~evaluateGenomeAll2.action={arg item;
			~fonctionRecordScore.value("~evaluateGenomeAll", 1);
			if(~agents != 0 ,{~fonctionUpdateGenomeAll.value});// Update Display
		};
		~validGenomeAll2 = Button(~wEditor,Rect(0, 0, 250, 18)).states=[["Validation Genomes", Color.black, Color.blue(0.8, 0.25)]];
		~validGenomeAll2.action = {arg view;
			var genomeAll=[], genomeAgent=[];
			// Validation genomes
			~agents.do({arg agent;
				var envTime, envLevel, env=[];
				genomeAgent=[];
				listeView.do({arg view, item;
					if(view != nil, {
						// Cas Speciaux
						if(item == 4 or: {item == 5}, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 127)});
						if(item == 6, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 127 / 254)});
						if(item == 11, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 16 / 80)});
						if(item == 12, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 13, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / ~tempsmaxsignal)});
						if(item == 14, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~displaySons.size - 1).max(1))});
						if(item == 15, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 16, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 18, {env=view.string.interpret.wrapAt(agent);
							envTime = env.value.wrapAt(0);
							envLevel = env.value.wrapAt(1);
							forBy(18, 25, 1 , {arg i; genomeAgent=genomeAgent.add(envLevel.wrapAt(i-18))});
							forBy(26, 33, 1 , {arg i; genomeAgent=genomeAgent.add(envTime.wrapAt(i-26))});
						});// Special Envelopes
						if(item == 34, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~listSynth.size - 1))});
						if(item == 38, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / ~sourceOutAgents.size)});
						if(item == 39, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 16)});
						//if(item == 40, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent))});
						if(item == 41, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 42, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 256)});
						if(item == 43, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 12)});
						if(item == 45, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~listeAlgorithm.size - 1).max(1))});
						// Cas Normaux
						if(item != 4 and: {item != 5} and: {item != 6} and: {item != 11} and: {item != 12} and: {item != 13} and: {item != 14} and: {item != 15} and: {item != 16} and: {item != 18} and: {item != 34} and: {item != 38} and: {item != 39} and: {item != 41} and: {item != 42} and: {item != 43} and: {item != 45}, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent))});
					});
				});
				genomeAll=genomeAll.add(genomeAgent);
			});
			~genomes=genomeAll;
			~fonctionRecordScore.value("~validGenomeAll", ~genomes);
		};
		~startTdefGenomeAll2 = Button(~wEditor,Rect(10, 10, 100, 18)).states=[["Tdef Display Off", Color.black, Color.green(0.8, 0.25)],["Tdef Display On", Color.black, Color.red(0.8, 0.25)]];
		//~startTdefGenomeAll2.action = {arg view;
		//if (view.value == 1, {// Start Tdef
		//~routineGenomeAll.play(quant: Quant.new(1, 0.0, 0.0));
		//~startTdefGenomeAll.valueAction_(1);
		//~startTdefGenomeAll3.valueAction_(1);
		//~startTdefGenomeAll4.valueAction_(1);
		//},
		//{// Stop Tdef
		//~routineGenomeAll.stop;
		//~startTdefGenomeAll.valueAction_(0);
		//~startTdefGenomeAll3.valueAction_(0);
		//~startTdefGenomeAll4.valueAction_(0);
		//});
		//};
		~startTdefGenomeAll2.enabled_(false);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Duration Low").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag9 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag9.hasVerticalScroller_(true);
		~ag9.hasHorizontalScroller_(true);
		~ag9.autohidesScrollers_(true);
		listeView=listeView.add(~ag9);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Duration Range").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag10 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag10.hasVerticalScroller_(true);
		~ag10.hasHorizontalScroller_(true);
		~ag10.autohidesScrollers_(true);
		listeView=listeView.add(~ag10);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Duration Mul").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag11 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag11.hasVerticalScroller_(true);
		~ag11.hasHorizontalScroller_(true);
		~ag11.autohidesScrollers_(true);
		listeView=listeView.add(~ag11);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Panoramic").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag12 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag12.hasVerticalScroller_(true);
		~ag12.hasHorizontalScroller_(true);
		~ag12.autohidesScrollers_(true);
		listeView=listeView.add(~ag12);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Buffer Duration").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag13 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag13.hasVerticalScroller_(true);
		~ag13.hasHorizontalScroller_(true);
		~ag13.autohidesScrollers_(true);
		listeView=listeView.add(~ag13);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Sounds").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag14 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag14.hasVerticalScroller_(true);
		~ag14.hasHorizontalScroller_(true);
		~ag14.autohidesScrollers_(true);
		listeView=listeView.add(~ag14);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Reverse Sound").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag15 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag15.hasVerticalScroller_(true);
		~ag15.hasHorizontalScroller_(true);
		~ag15.autohidesScrollers_(true);
		listeView=listeView.add(~ag15);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Loop Sound").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag16 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag16.hasVerticalScroller_(true);
		~ag16.hasHorizontalScroller_(true);
		~ag16.autohidesScrollers_(true);
		listeView=listeView.add(~ag16);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Offset Sound").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag17 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag17.hasVerticalScroller_(true);
		~ag17.hasHorizontalScroller_(true);
		~ag17.autohidesScrollers_(true);
		listeView=listeView.add(~ag17);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Envelopes").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag18 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag18.hasVerticalScroller_(true);
		~ag18.hasHorizontalScroller_(true);
		~ag18.autohidesScrollers_(true);
		listeView=listeView.add(~ag18);
		~wEditor.view.decorator.nextLine;
		forBy(19, 33, 1, {listeView=listeView.add(nil)});
		~wEditor.view.decorator.nextLine;
		// menu Genome
		~menuGenomeAll3 = PopUpMenu(~wEditor,Rect(0, 0, 125, 18)).background_(Color.grey(0.5, 0.8)).items = ["Genome Menu", "Load Genomes", "Save Genomes"];
		~menuGenomeAll3.action={arg item, file;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load Genomes
				1, {~userOperatingSystem.valueAction_(6)},
				// Save Genomes
				2, {~userOperatingSystem.valueAction_(12)}
			);
			~menuGenomeAll.valueAction_(0);
		};
		// Evaluate Genome
		~evaluateGenomeAll3 = Button(~wEditor, Rect(10, 10, 250, 18)).states=[["Evaluation Genomes", Color.black, Color.green(0.8, 0.25)]];
		~evaluateGenomeAll3.action={arg item;
			~fonctionRecordScore.value("~evaluateGenomeAll", 1);
			if(~agents != 0 ,{~fonctionUpdateGenomeAll.value});// Update Display
		};
		~validGenomeAll3 = Button(~wEditor,Rect(0, 0, 250, 18)).states=[["Validation Genomes", Color.black, Color.blue(0.8, 0.25)]];
		~validGenomeAll3.action = {arg view;
			var genomeAll=[], genomeAgent=[];
			// Validation genomes
			~agents.do({arg agent;
				var envTime, envLevel, env=[];
				genomeAgent=[];
				listeView.do({arg view, item;
					if(view != nil, {
						// Cas Speciaux
						if(item == 4 or: {item == 5}, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 127)});
						if(item == 6, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 127 / 254)});
						if(item == 11, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 16 / 80)});
						if(item == 12, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 13, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / ~tempsmaxsignal)});
						if(item == 14, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~displaySons.size - 1).max(1))});
						if(item == 15, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 16, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 18, {env=view.string.interpret.wrapAt(agent);
							envTime = env.value.wrapAt(0);
							envLevel = env.value.wrapAt(1);
							forBy(18, 25, 1 , {arg i; genomeAgent=genomeAgent.add(envLevel.wrapAt(i-18))});
							forBy(26, 33, 1 , {arg i; genomeAgent=genomeAgent.add(envTime.wrapAt(i-26))});
						});// Special Envelopes
						if(item == 34, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~listSynth.size - 1))});
						if(item == 38, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / ~sourceOutAgents.size)});
						if(item == 39, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 16)});
						//if(item == 40, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent))});
						if(item == 41, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 42, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 256)});
						if(item == 43, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 12)});
						if(item == 45, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~listeAlgorithm.size - 1).max(1))});
						// Cas Normaux
						if(item != 4 and: {item != 5} and: {item != 6} and: {item != 11} and: {item != 12} and: {item != 13} and: {item != 14} and: {item != 15} and: {item != 16} and: {item != 18} and: {item != 34} and: {item != 38} and: {item != 39} and: {item != 41} and: {item != 42} and: {item != 43} and: {item != 45}, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent))});
					});
				});
				genomeAll=genomeAll.add(genomeAgent);
			});
			~genomes=genomeAll;
			~fonctionRecordScore.value("~validGenomeAll", ~genomes);
		};
		~startTdefGenomeAll3 = Button(~wEditor,Rect(10, 10, 100, 18)).states=[["Tdef Display Off", Color.black, Color.green(0.8, 0.25)],["Tdef Display On", Color.black, Color.red(0.8, 0.25)]];
		//~startTdefGenomeAll3.action = {arg view;
		//if (view.value == 1, {// Start Tdef
		//~routineGenomeAll.play(quant: Quant.new(1, 0.0, 0.0));
		//~startTdefGenomeAll.valueAction_(1);
		//~startTdefGenomeAll2.valueAction_(1);
		//~startTdefGenomeAll4.valueAction_(1);
		//},
		//{// Stop Tdef
		//~routineGenomeAll.stop;
		//~startTdefGenomeAll.valueAction_(0);
		//~startTdefGenomeAll2.valueAction_(0);
		//~startTdefGenomeAll4.valueAction_(0);
		//});
		//};
		~startTdefGenomeAll3.enabled_(false);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Synthesizeur").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag34 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag34.hasVerticalScroller_(true);
		~ag34.hasHorizontalScroller_(true);
		~ag34.autohidesScrollers_(true);
		listeView=listeView.add(~ag34);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Control 1").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag35 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag35.hasVerticalScroller_(true);
		~ag35.hasHorizontalScroller_(true);
		~ag35.autohidesScrollers_(true);
		listeView=listeView.add(~ag35);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Control 2").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag36 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag36.hasVerticalScroller_(true);
		~ag36.hasHorizontalScroller_(true);
		~ag36.autohidesScrollers_(true);
		listeView=listeView.add(~ag36);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Control 3").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag37 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag37.hasVerticalScroller_(true);
		~ag37.hasHorizontalScroller_(true);
		~ag37.autohidesScrollers_(true);
		listeView=listeView.add(~ag37);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Audio Out").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag38 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag38.hasVerticalScroller_(true);
		~ag38.hasHorizontalScroller_(true);
		~ag38.autohidesScrollers_(true);
		listeView=listeView.add(~ag38);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Midi Out").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag39 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag39.hasVerticalScroller_(true);
		~ag39.hasHorizontalScroller_(true);
		~ag39.autohidesScrollers_(true);
		listeView=listeView.add(~ag39);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Audio In").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag40 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag40.hasVerticalScroller_(true);
		~ag40.hasHorizontalScroller_(true);
		~ag40.autohidesScrollers_(true);
		listeView=listeView.add(~ag40);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Loop Music").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag41 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag41.hasVerticalScroller_(true);
		~ag41.hasHorizontalScroller_(true);
		~ag41.autohidesScrollers_(true);
		listeView=listeView.add(~ag41);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Buffer Music").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag42 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag42.hasVerticalScroller_(true);
		~ag42.hasHorizontalScroller_(true);
		~ag42.autohidesScrollers_(true);
		listeView=listeView.add(~ag42);
		~wEditor.view.decorator.nextLine;
		// menu Genome
		~menuGenomeAll4 = PopUpMenu(~wEditor,Rect(0, 0, 125, 18)).background_(Color.grey(0.5, 0.8)).items = ["Genome Menu", "Load Genomes", "Save Genomes"];
		~menuGenomeAll4.action={arg item, file;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load Genomes
				1, {~userOperatingSystem.valueAction_(6)},
				// Save Genomes
				2, {~userOperatingSystem.valueAction_(12)}
			);
			~menuGenomeAll.valueAction_(0);
		};
		// Evaluate Genome
		~evaluateGenomeAll4 = Button(~wEditor, Rect(10, 10, 250, 18)).states=[["Evaluation Genomes", Color.black, Color.green(0.8, 0.25)]];
		~evaluateGenomeAll4.action={arg item;
			~fonctionRecordScore.value("~evaluateGenomeAll", 1);
			if(~agents != 0 ,{~fonctionUpdateGenomeAll.value});// Update Display
		};
		~validGenomeAll4 = Button(~wEditor,Rect(0, 0, 250, 18)).states=[["Validation Genomes", Color.black, Color.blue(0.8, 0.25)]];
		~validGenomeAll4.action = {arg view;
			var genomeAll=[], genomeAgent=[];
			// Validation genomes
			~agents.do({arg agent;
				var envTime, envLevel, env=[];
				genomeAgent=[];
				listeView.do({arg view, item;
					if(view != nil, {
						// Cas Speciaux
						if(item == 4 or: {item == 5}, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 127)});
						if(item == 6, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 127 / 254)});
						if(item == 11, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 16 / 80)});
						if(item == 12, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 13, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / ~tempsmaxsignal)});
						if(item == 14, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~displaySons.size - 1).max(1))});
						if(item == 15, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 16, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 18, {env=view.string.interpret.wrapAt(agent);
							envTime = env.value.wrapAt(0);
							envLevel = env.value.wrapAt(1);
							forBy(18, 25, 1 , {arg i; genomeAgent=genomeAgent.add(envLevel.wrapAt(i-18))});
							forBy(26, 33, 1 , {arg i; genomeAgent=genomeAgent.add(envTime.wrapAt(i-26))});
						});// Special Envelopes
						if(item == 34, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~listSynth.size - 1))});
						if(item == 38, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / ~sourceOutAgents.size)});
						if(item == 39, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 16)});
						//if(item == 40, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent))});
						if(item == 41, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) + 1 / 2)});
						if(item == 42, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 256)});
						if(item == 43, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / 12)});
						if(item == 45, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent) / (~listeAlgorithm.size - 1).max(1))});
						// Cas Normaux
						if(item != 4 and: {item != 5} and: {item != 6} and: {item != 11} and: {item != 12} and: {item != 13} and: {item != 14} and: {item != 15} and: {item != 16} and: {item != 18} and: {item != 34} and: {item != 38} and: {item != 39} and: {item != 41} and: {item != 42} and: {item != 43} and: {item != 45}, {genomeAgent=genomeAgent.add(view.string.interpret.wrapAt(agent))});
					});
				});
				genomeAll=genomeAll.add(genomeAgent);
			});
			~genomes=genomeAll;
			~fonctionRecordScore.value("~validGenomeAll", ~genomes);
		};
		~startTdefGenomeAll4 = Button(~wEditor,Rect(10, 10, 100, 18)).states=[["Tdef Display Off", Color.black, Color.green(0.8, 0.25)],["Tdef Display On", Color.black, Color.red(0.8, 0.25)]];
		~startTdefGenomeAll4.action = {arg view;
			if (view.value == 1, {// Start Tdef
				~routineGenomeAll.play(quant: Quant.new(1, 0.0, 0.0));
				~startTdefGenomeAll.valueAction_(1);
				~startTdefGenomeAll2.valueAction_(1);
				~startTdefGenomeAll3.valueAction_(1);
			},
			{// Stop Tdef
				~routineGenomeAll.stop;
				~startTdefGenomeAll.valueAction_(0);
				~startTdefGenomeAll2.valueAction_(0);
				~startTdefGenomeAll3.valueAction_(0);
			});
		};
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Chord Max").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag43 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag43.hasVerticalScroller_(true);
		~ag43.hasHorizontalScroller_(true);
		~ag43.autohidesScrollers_(true);
		listeView=listeView.add(~ag43);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Chord Dur").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag44 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag44.hasVerticalScroller_(true);
		~ag44.hasHorizontalScroller_(true);
		~ag44.autohidesScrollers_(true);
		listeView=listeView.add(~ag44);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Algorithm").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag45 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag45.hasVerticalScroller_(true);
		~ag45.hasHorizontalScroller_(true);
		~ag45.autohidesScrollers_(true);
		listeView=listeView.add(~ag45);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_("Fhz Band").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~ag46 = TextView(~wEditor, Rect(0, 0, 750, 50));
		~ag46.hasVerticalScroller_(true);
		~ag46.hasHorizontalScroller_(true);
		~ag46.autohidesScrollers_(true);
		listeView=listeView.add(~ag46);
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 300, 18)).string_(" ").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));

		~wEditor.onClose_({~listeWindows.removeAt(~positionWindow); ~indexwindow=0});

		~wEditor.front;

		//Setup Font
		~wEditor.view.do({arg view;
			view.children.do({arg subView;
				subView.font = Font("Helvetica", 12);
			});
		});

		~listeWindows=~listeWindows.add(~wEditor);
		~positionWindow = ~listeWindows.size - 1;
		~fonctionShortCuts.value(~wEditor);

		// PROCESSUS UPDATE AFFICHAGE GENOMES
		~routineGenomeAll=Tdef(\GenomeAllDisplay, {
			loop({
				// Update affiche genome
				{
					if(~agents != 0 ,{~fonctionUpdateGenomeAll.value});// Update Display
				}.defer;
				1.wait;
			});
		});

		~fonctionUpdateGenomeAll={
			var gene=0;
			listeView.do({arg v;
				var genome=[], env=[];
				if(v != nil, {
					// Cas Speciaux
					if(gene == 4 or: {gene == 5}, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString((genome.round(0.0001) * 127).asCompileString, 0, 999999999)});
					if(gene == 6, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString((genome.round(0.0001) * 254 - 127).asCompileString, 0, 999999999)});
					if(gene == 11, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString((genome.round(0.0001) * 80 - 16).asCompileString, 0, 999999999)});
					if(gene == 12, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString((genome.round(0.0001) * 2 - 1).asCompileString, 0, 999999999)});
					if(gene == 13, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString((genome.round(0.0001) * ~tempsmaxsignal).asCompileString, 0, 999999999)});
					if(gene == 14, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString(((genome.round(0.0001) * ~displaySons.size - 1).ceil.clip(0, ~displaySons.size - 1)).asCompileString, 0, 999999999)});
					if(gene == 15, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString((genome.round(0.0001) * 2 - 1).asCompileString, 0, 999999999)});
					if(gene == 16, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString((genome.round(0.0001) * 2 - 1).asCompileString, 0, 999999999)});
					if(gene == 18, {~agents.do({arg agent; env=env.add([~genomes.wrapAt(agent).copyRange(26, 33), ~genomes.wrapAt(agent).copyRange(18, 25)])});
						v.setString(env.round(0.0001).asCompileString, 0, 999999999)});
					if(gene == 34, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString(((genome.round(0.0001) * ~listSynth.size - 1).ceil.clip(0, ~listSynth.size - 1)).asCompileString, 0, 999999999)});
					if(gene == 38, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString(((genome.round(0.0001) * (~sourceOutAgents.size - 1)).ceil.clip(0, ~sourceOutAgents.size - 1)).asCompileString, 0, 999999999)});
					if(gene == 39, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString((genome.round(0.0001) * 16).asCompileString, 0, 999999999)});
					/*if(gene == 40, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString(((genome.round(0.0001) * (~audioInLR.size - 1)).ceil.clip(0, ~audioInLR.size - 1)).asCompileString, 0, 999999999)});*/
					if(gene == 41, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString((genome.round(0.0001) * 2 - 1).asCompileString, 0, 999999999)});
					if(gene == 42, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString(((genome.round(0.0001) * 256).floor.clip(1, 256)).asCompileString, 0, 999999999)});
					if(gene == 43, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString(((genome.round(0.0001) * 12).floor.clip(1, 12)).asCompileString, 0, 999999999)});
					if(gene == 45, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString(((genome.round(0.0001) * ~listeAlgorithm.size - 1).ceil.clip(0, ~listeAlgorithm.size - 1)).asCompileString, 0, 999999999)});
					// Cas Normaux
					if(gene != 4 and: {gene != 5} and: {gene != 6} and: {gene != 11} and: {gene != 12} and: {gene != 13} and: {gene != 14} and: {gene != 15} and: {gene != 16} and: {gene != 18} and: {gene != 34} and: {gene != 38} and: {gene != 39} and: {gene != 41} and: {gene != 42} and: {gene != 43} and: {gene != 45}, {~agents.do({arg agent; genome=genome.add(~genomes.wrapAt(agent).wrapAt(gene))});
						v.setString(genome.round(0.0001).asCompileString, 0, 999999999)});
				});
				gene=gene+1});
		};

		if(~agents != 0 ,{~fonctionUpdateGenomeAll.value});// Update Display first time

	}

}

