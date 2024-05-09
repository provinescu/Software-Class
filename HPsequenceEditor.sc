// A Software by Herve Provini

HPsequenceEditor {

	*new	{arg agent=0;

		^super.new.init(agent);

	}

	init	{arg agent;

		// Init GUI + edit sequences
		// utilise les variables ~listeagentfreq ~listeagentamp ~listeagentduree et ~agents de Agents !!!!

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system

		~flagHPsequenceEditor = 'on';

		this.edit(agent);

	}

	edit {arg agent, listeView=[];

		~flagSequence=999;
		~agentSequence=1;

		//Activation sequence Agents-Synth
		~wSequence = Window("Sequences Editor for Agents by HP", Rect(500, 100, 625, 500));
		~wSequence.view.decorator = FlowLayout(~wSequence.view.bounds);
		StaticText(~wSequence, Rect(0, 0, 500, 24)).string_("A Sequences Editor for Agents").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 14));
		~wSequence.view.decorator.nextLine;
		// menu Sequence
		~menuSequence = PopUpMenu(~wSequence,Rect(0, 0, 125, 20)).background_(Color.grey(0.5, 0.8)).items = ["Sequences Menu", "Load Sequence", "Load Sequences", "Save Sequence", "Save Sequences"];
		~menuSequence.action={arg item, file;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load Sequences
				2, {~userOperatingSystem.valueAction_(7)},
				// Save Sequences
				4, {~userOperatingSystem.valueAction_(13)},
				// Load Sequence
				1, {Dialog.openPanel({ arg paths;
					paths.do({ arg p; var file, seq;
						file=File(p,"r");
						seq = file.readAllString.interpret;file.close;
						~listeagentfreq.wrapPut(~agentSequence-1, seq.wrapAt(0));~listeagentamp.wrapPut(~agentSequence-1, seq.wrapAt	(1));~listeagentduree.wrapPut(~agentSequence-1, seq.wrapAt(2))})},{"cancelled".postln})},
				// Save Sequence
				3, {Dialog.savePanel({arg path; var file, seq;
					file=File(path++".scd","w");file.write([~listeagentfreq.wrapAt(~agentSequence-1),~listeagentamp.wrapAt(~agentSequence-1),~listeagentduree.wrapAt(~agentSequence-1)].asCompileString);file.close},{"cancelled".postln})}
			);
			~menuSequence.value_(0);
		};
		~menuSequence.focus;
		// Choix de l'agent et routine automation display
		StaticText(~wSequence, Rect(0, 0, 50, 18)).string_("Agent").stringColor_(Color.black(1.0,1.0));
		~agentNumberSeq=NumberBox(~wSequence, 25 @ 18);
		~agentNumberSeq.value = 1;
		~agentNumberSeq.action={|num| var number;
			~fonctionRecordScore.value("~agentNumberSeq", num.value);
			if(~agents != 0, {
				number = num.value.asInteger;
				if(number < 1, {number = ~agents;~agentNumberSeq.value_(~agents)});
				if(number > ~agents, {number = 1;~agentNumberSeq.value_(1)});
				~agentSequence = number;
				// Update Display
				~fonctionUpdateSequence.value(~agentSequence);
			}, {~agentNumberSeq.value_(0)});
		};
		// Copy Sequence
		~fonctionCopySequence = Button(~wSequence,Rect(0, 0, 150, 20)).states=[["-> Copy Sequence ->", Color.black, Color.yellow(0.8, 0.25)]];
		~fonctionCopySequence.action={arg item;
			~fonctionRecordScore.value("~fonctionCopySequence", item.value);
			if(~agentSequence >= 1 and: {~agentSequence <= ~agents},
				{~listeagentfreq.wrapPut(~agentCopySequence.value - 1, ~listeagentfreq.wrapAt(~agentSequence-1).copy);
					~listeagentamp.wrapPut(~agentCopySequence.value - 1, ~listeagentamp.wrapAt(~agentSequence-1).copy);
					~listeagentduree.wrapPut(~agentCopySequence.value - 1, ~listeagentduree.wrapAt(~agentSequence-1).copy)});
		};
		StaticText(~wSequence, Rect(0, 0, 50, 18)).string_("Agent").stringColor_(Color.black(1.0,1.0));
		~agentCopySequence=NumberBox(~wSequence, 25 @ 18);
		~agentCopySequence.value = 1;
		~agentCopySequence.action={|num|
			~fonctionRecordScore.value("~agentCopySequence", num.value);
			if(~agents != 0, {
				if(num.value < 1, {~agentCopySequence.value_(~agents)});
				if(num.value > ~agents, {~agentCopySequence.value_(1)});
			},{~agentCopySequence.value_(0)});
		};
		~startTdefSeq = Button(~wSequence,Rect(10, 10, 100, 18)).states=[["Tdef Display Off", Color.black, Color.green(0.8, 0.25)],["Tdef Display On", Color.black, Color.red(0.8, 0.25)]];
		~startTdefSeq.action = {arg view;
			if (view.value == 1, {// Start Tdef
				~routineSequence.play(quant: Quant.new(1, 0.0, 0.0));
			},
			{// Stop Tdef
				~routineSequence.stop;
			});
		};
		~wSequence.view.decorator.nextLine;
		~validSeqFreqAmpDur = Button(~wSequence,Rect(0, 0, 250, 20)).states=[["Validation Sequence Fhz/Amp/Time", Color.black, Color.blue(0.8, 0.25)]];
		~validSeqFreqAmpDur.action = {arg view;
			if(~agentSequence >= 1 and: {~agentSequence <= ~agents},
				{~listeagentfreq.wrapPut(~agentSequence-1, ~wSeqFreq.string.interpret/127);
					~listeagentamp.wrapPut(~agentSequence-1, ~wSeqAmp.string.interpret.dbamp);
					~listeagentduree.wrapPut(~agentSequence-1, ~wSeqDur.string.interpret)});
			~fonctionRecordScore.value("~validSeqFreqAmpDur", [~listeagentfreq, ~listeagentamp, ~listeagentduree]);
		};
		~wSequence.view.decorator.nextLine;
		StaticText(~wSequence, Rect(0, 0, 175, 24)).string_("FHZ Sequence").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 10));
		~wSeqFreq = TextView(~wSequence, Rect(0, 0, 600, 100));
		~wSeqFreq.hasVerticalScroller_(true);
		~wSeqFreq.hasHorizontalScroller_(true);
		~wSeqFreq.autohidesScrollers_(true);
		~wSequence.view.decorator.nextLine;
		StaticText(~wSequence, Rect(0, 0, 600, 24)).string_("AMP Sequence").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 10));
		~wSeqAmp = TextView(~wSequence, Rect(0, 0, 600, 100));
		~wSeqAmp.hasVerticalScroller_(true);
		~wSeqAmp.hasHorizontalScroller_(true);
		~wSeqAmp.autohidesScrollers_(true);
		~wSequence.view.decorator.nextLine;
		StaticText(~wSequence, Rect(0, 0, 600, 24)).string_("TIME Sequence").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 10));
		~wSeqDur = TextView(~wSequence, Rect(0, 0, 600, 100));
		~wSeqDur.hasVerticalScroller_(true);
		~wSeqDur.hasHorizontalScroller_(true);
		~wSeqDur.autohidesScrollers_(true);
		~wSequence.view.decorator.nextLine;

		~wSequence.onClose_({~listewindows.removeAt(~positionWindow); ~indexwindow=0});

		~wSequence.front;

		//Setup Font
		~wSequence.view.do({arg view;
			view.children.do({arg subView;
				subView.font = Font("Helvetica", 12);
			});
		});

		//Init ShortCuts
		~listeWindows=~listeWindows.add(~wSequence);
		~positionWindow = ~listeWindows.size - 1;
		~fonctionShortCuts.value(~wSequence);

		// PROCESSUS UPDATE AFFICHAGE GENOMES
		~routineSequence=Tdef(\SequenceDisplay, {
			loop({
				// Update affiche genome
				{if(~agents != 0, {
					if(~agents < ~agentSequence, {~agentNumberSeq.value_(~agents)});
					if(~agents < ~agentCopySequence.value, {~agentCopySequence.value_(~agents)});
					if(~agentSequence == 0 , {~agentNumberSeq.value_(1);~agentCopySequence.value_(1)});
					~fonctionUpdateSequence.value(~agentSequence);// Update Display
				}, {~agentNumberSeq.value_(0);~agentCopySequence.value_(0)});
				}.defer;
				1.wait;
			});
		});

		~fonctionUpdateSequence={arg agent;
			if(agent >= 1 and: {agent <= ~agents},
				{~wSeqFreq.setString((~listeagentfreq.wrapAt(agent-1)*127).asCompileString, 0, 999999999);
					~wSeqAmp.setString(~listeagentamp.wrapAt(agent-1).ampdb.asCompileString, 0, 999999999);
					~wSeqDur.setString(~listeagentduree.wrapAt(agent-1).asCompileString, 0, 999999999)});
		};

	}

}

