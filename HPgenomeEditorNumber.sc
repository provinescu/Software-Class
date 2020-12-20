// A Software by Herve Provini

HPgenomeEditorNumber {

	*new	{arg agent=0;

		^super.new.init(agent);

	}

	init	{arg agent;

		// Init GUI + edit sequences
		// utilise ~genomes et ~agents de AgentsBand !!!!

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system

		~flagHPgenomeEditor = 'on';

		this.edit(agent);

	}

	edit {arg agent, genome, listeView=[];

		~flagGene=999;
		~agentEditor=1;

		//Activation genomes Agents-Synth
		~wEditor = Window("Genomes Editor for AgentsBand by HP", Rect(10, 10, 625, 550), scroll: true);
		~wEditor.view.decorator = FlowLayout(~wEditor.view.bounds);
		StaticText(~wEditor, Rect(0, 0, 500, 24)).string_("A Genomes Editor for AgentsBand").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 14));
		~wEditor.view.decorator.nextLine;
		// Menu Genome
		~menuGenome = PopUpMenu(~wEditor,Rect(0, 0, 150, 20)).background_(Color.grey(0.5, 0.8)).items = ["Genomes Menu", "Load Genome",  "Load Genomes", "Save Genome", "Save Genomes"];
		~menuGenome.action={arg item, file;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load Genomes
				2, {~userOperatingSystem.valueAction_(6);~menuGenome.valueAction_(0)},
				// Save Genomes
				4, {~userOperatingSystem.valueAction_(12);~menuGenome.valueAction_(0)},
				// Load Genome
				1, {Dialog.openPanel({ arg p, file, genome;
					file=File(p,"r");
					genome = file.readAllString.interpret;file.close;
					~genomes.wrapPut(~agentEditor-1, genome);~menuGenome.valueAction_(0)},{"cancelled".postln;~menuGenome.valueAction_(0)})},
				// Save Genome
				3, {Dialog.savePanel({arg path; var file, genome;
					genome=~genomes.wrapAt(~agentEditor-1);
					file=File(path++".scd","w");file.write(genome.asCompileString);file.close;~menuGenome.valueAction_(0)},	{"cancelled".postln;~menuGenome.valueAction_(0)})}
			);
		};
		~menuGenome.focus;
		// Choix de l'agent et routine automation display
		StaticText(~wEditor, Rect(0, 0, 50, 18)).string_("Agent").stringColor_(Color.black(1.0,1.0));
		~agentNumber=NumberBox(~wEditor, 25 @ 18);
		~agentNumber.value = 1;
		~agentNumber.action={|num| var number;
			~fonctionRecordScore.value("~agentNumber", num.value);
			if(~agents != 0, {
				number = num.value.asInteger;
				if(number < 1, {number = ~agents;~agentNumber.value_(~agents)});
				if(number > ~agents, {number = 1;~agentNumber.value_(1)});
				~agentEditor = number;
				~fonctionUpdateGenome.value(~agentEditor);// Update Display
			},{~agentNumber.value_(0)});
		};
		// Copy Genome
		~fonctionCopyGenome = Button(~wEditor,Rect(0, 0, 175, 20)).states=[["->   Copy Genome   ->", Color.black, Color.yellow(0.8, 0.25)]];
		~fonctionCopyGenome.action={arg item;
			~fonctionRecordScore.value("~fonctionCopyGenome", 1);
			if(~agentEditor >= 1 and: {~agentEditor <= ~agents},
				{~genomes.wrapPut(~agentCopy.value - 1, ~genomes.wrapAt(~agentEditor-1).copy)});
		};
		StaticText(~wEditor, Rect(0, 0, 50, 18)).string_("Agent").stringColor_(Color.black(1.0,1.0));
		~agentCopy=NumberBox(~wEditor, 25 @ 18);
		~agentCopy.value = 1;
		~agentCopy.action={|num|
			~fonctionRecordScore.value("~agentCopy", num.value);
			if(~agents != 0 and: {~genomes != []}, {
				if(num.value < 1, {~agentCopy.value_(~agents)});
				if(num.value > ~agents, {~agentCopy.value_(1)});
			},{~agentCopy.value_(0)});
		};
		~startTdef = Button(~wEditor,Rect(10, 10, 100, 18)).states=[["Tdef Display Off", Color.black, Color.green(0.8, 0.25)],["Tdef Display On", Color.black, Color.red(0.8, 0.25)]];
		~startTdef.action = {arg view;
			if (view.value == 1, {// Start Tdef
				~routineGenome.play(quant: Quant.new(1, 0.0, 0.0));
			},
			{// Stop Tdef
				~routineGenome.stop;
			});
		};
		~wEditor.view.decorator.nextLine;
		// Bio Genome
		StaticText(~wEditor, Rect(0, 0, 500, 18)).string_("Bio Genome").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~wEditor.view.decorator.nextLine;
		~ag0 = EZNumber(~wEditor, 150@18, "Ageing", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag0", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(0, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=0;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(0), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag0);
		~ag1 = EZNumber(~wEditor, 150@18, "Moving", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag1", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(1, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=1;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(1), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag1);
		~ag2 = EZNumber(~wEditor, 150@18, "Agent Vision", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag2", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(2, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=2;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(2), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag2);
		~ag3 = EZNumber(~wEditor, 150@18, "Signal Vision", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag3", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(3, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=3;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(3), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag3);
		~wEditor.view.decorator.nextLine;
		// Music Genome
		StaticText(~wEditor, Rect(0, 0, 500, 18)).string_("Music Genome").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~wEditor.view.decorator.nextLine;
		~ag4 = EZNumber(~wEditor, 150@18, "Fhz Low", ControlSpec(0, 127, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag4", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(4, view.value/127);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=4;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(4)*127, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag4);
		~ag5 = EZNumber(~wEditor, 150@18, "Fhz Range", ControlSpec(0, 127, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag5", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(5, view.value/127);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=5;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(5)*127, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag5);
		~ag6 = EZNumber(~wEditor, 150@18, "Translate Fhz", ControlSpec(-127, 127, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag6", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(6, view.value + 127 / 254);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=6;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(6) * 254 - 127, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag6);
		~wEditor.view.decorator.nextLine;
		~ag7 = EZNumber(~wEditor, 150@18, "Amplitude Low", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag7", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(7, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=7;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(7), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag7);
		~ag8 = EZNumber(~wEditor, 150@18, "Amplitude Range", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag8", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(8, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=8;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(8), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag8);
		~wEditor.view.decorator.nextLine;
		~ag9 = EZNumber(~wEditor, 150@18, "Duration Low", ControlSpec(0, 60, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag9", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(9, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=9;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(9), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag9);
		~ag10= EZNumber(~wEditor, 150@18, "Duration Range", ControlSpec(0, 60, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag10", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(10, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=10;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(10), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag10);
		~ag11= EZNumber(~wEditor, 150@18, "Duration Mul", ControlSpec(-16, 64, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag11", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(11, view.value + 16 /80);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=11;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(11) * 80 - 16, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag11);
		~wEditor.view.decorator.nextLine;
		// Panoramic Genome
		StaticText(~wEditor, Rect(0, 0, 500, 18)).string_("Panoramic Genome").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~wEditor.view.decorator.nextLine;
		~ag12= EZNumber(~wEditor, 150@18, "Panoramic", ControlSpec(-1, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag12", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(12, view.value + 1 / 2);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=12;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(12) * 2 - 1, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag12);
		~wEditor.view.decorator.nextLine;
		// Sample Genome
		StaticText(~wEditor, Rect(0, 0, 500, 18)).string_("Sample Buffer Genome").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~wEditor.view.decorator.nextLine;
		~ag13= EZNumber(~wEditor, 150@18, "Buffer Duration", ControlSpec(0.015625, ~tempsmaxsignal, \lin, 0),
			{arg view;var dureeBuffer;
				~fonctionRecordScore.value("~ag13", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(13, view.value / ~tempsmaxsignal);~genomes.wrapPut(~agentEditor-1, genome);
				// Init Buffer
				~bufferAudioAgents.wrapAt(~agentEditor-1 ).free;
				~bufferFileAgents.wrapAt(~agentEditor-1 ).free;
				dureeBuffer = (Server.default.sampleRate * ~genomes.wrapAt(~agentEditor-1 ).wrapAt(13)*~tempsmaxsignal);
				~bufferAudioAgents.wrapPut(~agentEditor-1, Buffer.alloc(Server.default, dureeBuffer));
				~bufferFileAgents.wrapPut(~agentEditor-1, Buffer.alloc(Server.default, dureeBuffer));
				~flagGene=13;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(13) * ~tempsmaxsignal, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag13);
		StaticText(~wEditor, Rect(0, 0, 50, 18)).string_("Sounds");
		~ag14= PopUpMenu(~wEditor, 190@18).items_(~displaySons);
		~ag14.action={arg son;
			~fonctionRecordScore.value("~ag14", son.value);
			genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(14, son.value / (~displaySons.size - 1).max(1));~genomes.wrapPut(~agentEditor-1, genome);
			~flagGene=14;
		};
		listeView=listeView.add(~ag14);
		~wEditor.view.decorator.nextLine;
		~ag15= EZNumber(~wEditor, 150@18, "Reverse Sound", ControlSpec(-1, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag15", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(15, view.value + 1 / 2);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=15;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(15) * 2 - 1, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag15);
		~ag16= EZNumber(~wEditor, 150@18, "Loop Sound", ControlSpec(-1, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag16", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(16, view.value + 1 / 2);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=16;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(16) * 2 - 1, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag16);
		~ag17= EZNumber(~wEditor, 150@18, "Offset Sound", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag17", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(17, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=17;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(17), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag17);
		~wEditor.view.decorator.nextLine;
		// Env Genome
		StaticText(~wEditor, Rect(0, 0, 500, 18)).string_("Envelopes Genome").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~wEditor.view.decorator.nextLine;
		~ag18=EnvelopeView(~wEditor, Rect(0, 0, 615, 80));
		~ag18.drawLines_(true);
		~ag18.selectionColor_(Color.red);
		~ag18.fillColor_(Color(0, 0.25, 0.5));
		~ag18.strokeColor_(Color.cyan);
		~ag18.drawRects_(true);
		~ag18.step_(0.001);
		~ag18.gridOn_(true);
		~ag18.resize_(5);
		~ag18.thumbSize_(12.0);
		~ag18.value_([[0.0, 0.015625, 0.125, 0.375, 0.625, 0.75, 0.875, 1.0], [0.0, 1.0, 1.0, 0.75, 0.75, 0.5, 0.5, 0.0]]);
		~ag18.gridColor_(Color.grey);
		~ag18.action={arg env; var envTime, envLevel;
			~fonctionRecordScore.value("~ag18", env.value);
			envTime = env.value.wrapAt(0);
			envLevel = env.value.wrapAt(1);
			genome=~genomes.wrapAt(~agentEditor-1);
			forBy(18, 25, 1 , {arg i; genome.wrapPut(i, envLevel.wrapAt(i-18))});
			forBy(26, 33, 1 , {arg i; genome.wrapPut(i, envTime.wrapAt(i-26))});
			~genomes.wrapPut(~agentEditor-1, genome);
			~flagGene=18;
		};
		listeView=listeView.add(~ag18);
		forBy(19, 33, 1, {listeView=listeView.add(nil)});
		~wEditor.view.decorator.nextLine;
		// Synth Genome
		StaticText(~wEditor, Rect(0, 0, 500, 18)).string_("Synthesizeur Genome").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~wEditor.view.decorator.nextLine;
		StaticText(~wEditor, Rect(0, 0, 75, 18)).string_("Synthesizeur");
		~ag34= PopUpMenu(~wEditor, 190@18).items_(~listSynth);
		~ag34.action={arg synth;
			~fonctionRecordScore.value("~ag34", synth.value);
			genome=~genomes.wrapAt(~agentEditor-1);genome.wrapPut(34, synth.value / (~listSynth.size - 1));~genomes.wrapPut(~agentEditor-1, genome);
			~flagGene=34;
		};
		listeView=listeView.add(~ag34);
		~ag35= EZNumber(~wEditor, 110@18, "Control 1", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag35", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(35, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=35;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(35), labelWidth:50, numberWidth:50);
		listeView=listeView.add(~ag35);
		~ag36= EZNumber(~wEditor, 110@18, "Control 2", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag36", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(36, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=36;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(36), labelWidth:50, numberWidth:50);
		listeView=listeView.add(~ag36);
		~ag37= EZNumber(~wEditor, 110@18, "Control 3", ControlSpec(0, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag37", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(37, view.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=37;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(37), labelWidth:50, numberWidth:50);
		listeView=listeView.add(~ag37);
		~wEditor.view.decorator.nextLine;
		// Space Genome
		StaticText(~wEditor, Rect(0, 0, 500, 18)).string_("Space Genome").stringColor_(Color.black(1.0,1.0)).font_(Font("Georgia", 12));
		~wEditor.view.decorator.nextLine;
		~ag38= EZNumber(~wEditor, 150@18, "Audio Out", ControlSpec(1, ~sourceOutAgents.size, \lin, 1),
			{arg view;
				~fonctionRecordScore.value("~ag38", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(38, view.value / ~sourceOutAgents.size);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=38;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(38) * ~sourceOutAgents.size, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag38);
		~ag39= EZNumber(~wEditor, 150@18, "Midi Out", ControlSpec(0, 16, \lin, 1),
			{arg view;
				~fonctionRecordScore.value("~ag39", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(39, view.value / 16);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=39;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(39) * 15 + 1, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag39);
		/*StaticText(~wEditor, Rect(0, 0, 50, 18)).string_("Audio In");
		~ag40= PopUpMenu(~wEditor, 60@18).items_(~audioInLR);
		~ag40.action={arg audio;
			~fonctionRecordScore.value("~ag40", audio.value);
			genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(40, audio.value);~genomes.wrapPut(~agentEditor-1, genome);
			~flagGene=40;
		};*/
		~ag40 = EZNumber(~wEditor, 150@18, "Audio In", ControlSpec(0, ~audioInLR.size - 1, \lin, 1),
			{arg view;
				~fonctionRecordScore.value("~ag40", view.value);
			genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(40, view.value);~genomes.wrapPut(~agentEditor-1, genome);
			~flagGene=40;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(40), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag40);
		~ag41= EZNumber(~wEditor, 150@18, "Loop Music", ControlSpec(-1, 1, \lin, 0),
			{arg view;
				~fonctionRecordScore.value("~ag41", view.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(41, view.value + 1 / 2);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=41;
			},
			~genomes.wrapAt(~agentEditor-1 ).wrapAt(41) * 2 - 1, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag41);
		~ag42= EZNumber(~wEditor, 150@18, "Buffer Music", ControlSpec(1, 256, \lin, 1),{arg view;
			~fonctionRecordScore.value("~ag42", view.value);
			genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(42, view.value  / 256);~genomes.wrapPut(~agentEditor-1, genome);
			if(~flagGeneBufferMusic == 'on', {~agents.do({arg agent;~bufferDataAgents.wrapPut(agent, (~genomes.wrapAt(agent).wrapAt(42) * 256).floor.clip(1, 256))})});
			~flagGene=42;
		},
		~genomes.wrapAt(~agentEditor-1 ).wrapAt(42) * 256, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag42);
		~ag43= EZNumber(~wEditor, 150@18, "Chord Max", ControlSpec(1, 12, \lin, 1),{arg view;
			~fonctionRecordScore.value("~ag43", view.value);
			genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(43, view.value  / 12);~genomes.wrapPut(~agentEditor-1, genome);
			if(~flagGeneChordMax == 'on', {~agents.do({arg agent;~chordMaxAgents.wrapPut(agent, (~genomes.wrapAt(agent).wrapAt(43) * 12).floor.clip(1, 12))})});
			~flagGene=43;
		},
		~genomes.wrapAt(~agentEditor-1 ).wrapAt(43) * 12, labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag43);
		~ag44= EZNumber(~wEditor, 150@18, "Chord Dur", ControlSpec(0.01, 1, \lin, 0),{arg view;
			~fonctionRecordScore.value("~ag44", view.value);
			genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(44, view.value);~genomes.wrapPut(~agentEditor-1, genome);
			if(~flagGeneChordMax == 'on', {~agents.do({arg agent;~chordDurAgents.wrapPut(agent, ~genomes.wrapAt(agent).wrapAt(44))})});
			~flagGene=44;
		},
		~genomes.wrapAt(~agentEditor-1 ).wrapAt(44), labelWidth:125, numberWidth:50);
		listeView=listeView.add(~ag44);
		StaticText(~wEditor, Rect(0, 0, 50, 18)).string_("Algorithm");
		~ag45= PopUpMenu(~wEditor, 100@18).items_(~listeAlgorithm);
		~ag45.action={arg algo;
			~fonctionRecordScore.value("~ag45", algo.value);
			genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(45, algo.value / (~listeAlgorithm.size - 1).max(1));~genomes.wrapPut(~agentEditor-1, genome);
			~flagGene=45;
		};
		listeView=listeView.add(~ag45);
		~ag46=EZText(~wEditor, Rect(0, 0, 475, 20), "Fhz Band",
			{arg algo;
				~fonctionRecordScore.value("~ag46", algo.value);
				genome=~genomes.wrapAt(~agentEditor-1 );genome.wrapPut(46, algo.value);~genomes.wrapPut(~agentEditor-1, genome);
				~flagGene=46;
		}, ~genomes.wrapAt(~agentEditor-1 ).at(46), true);
		listeView=listeView.add(~ag46);

		~wEditor.onClose_({~listeWindows.removeAt(~positionWindow); ~indexwindow=0});


		~wEditor.front;

		//Setup Font
		~wEditor.view.do({arg view;
			view.children.do({arg subView;
				subView.font = Font("Helvetica", 12);
			});
		});

		//Init ShortCuts
		~listeWindows=~listeWindows.add(~wEditor);
		~positionWindow = ~listeWindows.size - 1;
		~fonctionShortCuts.value(~wEditor);

		// PROCESSUS UPDATE AFFICHAGE GENOMES
		~routineGenome=Tdef(\GenomeDisplay, {
			loop({
				// Update affiche genome
				{if(~agents != 0, {
					if(~agents < ~agentEditor, {~agentNumber.value_(~agents)});
					if(~agents < ~agentCopy.value, {~agentCopy.value_(~agents)});
					if(~agentEditor == 0, {~agentNumber.value_(1);~agentCopy.value_(1)});
					~fonctionUpdateGenome.value(~agentEditor);// Update Display
				}, {~agentNumber.value_(0);~agentCopy.value_(0)});
				}.defer;
				1.wait;
			});
		});

		~fonctionUpdateGenome = { arg agent; var gene=0;
			if(agent >= 1 and: {agent <= ~agents},
				{listeView.do({arg v;
					if(v != nil, {
						// Cas Speciaux
						if(gene == 4 or: {gene == 5}, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene)*127)});
						if(gene == 6, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene) * 254 - 127)});
						if(gene == 11, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene) * 80 - 16)});
						if(gene == 12, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene) * 2 - 1)});
						if(gene == 13, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene) * ~tempsmaxsignal)});
						if(gene == 14, {v.value_((~genomes.wrapAt(agent-1).wrapAt(gene) * ~displaySons.size - 1).ceil.clip(0, ~displaySons.size - 1))});
						if(gene == 15, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene) * 2 - 1)});
						if(gene == 16, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene) * 2 - 1)});
						if(gene == 18, {v.valueAction_([~genomes.wrapAt(agent-1).copyRange(26, 33), ~genomes.wrapAt(agent-1).copyRange(18, 25)])});
						if(gene == 34, {v.value_((~genomes.wrapAt(agent-1).wrapAt(gene) * ~listSynth.size - 1).ceil.clip(0, ~listSynth.size - 1))});
						if(gene == 38, {v.valueAction_((~genomes.wrapAt(agent-1).wrapAt(gene) * (~sourceOutAgents.size - 1)).ceil.clip(0, ~sourceOutAgents.size - 1))});
						if(gene == 39, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene) * 16)});
						//if(gene == 40, {v.value_((~genomes.wrapAt(agent-1).wrapAt(gene) * (~audioInLR.size - 1)).ceil.clip(0, ~audioInLR.size - 1))});
						if(gene == 41, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene) * 2 - 1)});
						if(gene == 42, {v.valueAction_((~genomes.wrapAt(agent-1).wrapAt(gene) * 256).floor.clip(1, 256))});
						if(gene == 43, {v.valueAction_((~genomes.wrapAt(agent-1).wrapAt(gene) * 12).floor.clip(1, 12))});
						if(gene == 45, {v.value_((~genomes.wrapAt(agent-1).wrapAt(gene) * ~listeAlgorithm.size - 1).ceil.clip(0, ~listeAlgorithm.size - 1))});
						// Cas Normaux
						if(gene != 4 and: {gene != 5} and: {gene != 6} and: {gene != 11} and: {gene != 12} and: {gene != 13} and: {gene != 14} and: {gene != 15} and: {gene != 16} and: {gene != 18} and: {gene != 34} and: {gene != 38} and: {gene != 39} and: {gene != 41} and: {gene != 42} and: {gene != 43} and: {gene != 45}, {v.valueAction_(~genomes.wrapAt(agent-1).wrapAt(gene))});
					});
					gene=gene+1});
			});
		};

	}

}
