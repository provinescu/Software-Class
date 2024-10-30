// A Software by Herve Provini

HPmetronomeQT {


	classvar  <> server;

	var menuMetronome, patternFreq, patternFreqStart, patternFreqEnd, patternAmp, patternDur, metronome, bpmTempo, cmdperiodfunc, evaluationKeyDown, fonctionShortCuts, window, start, tapTempo, tempo, bpm, baresList, beatsList, bareMenu, beatMenu, patternFonction, freq, amp, dur, degrees, tuning, scaling, roots, bares, beats, tuningMode, tuningMenu, rootMenu, rootMode, degreesMenu, scale, patternAmpStart, patternAmpEnd;

	*new	{

		^super.new.init();

	}

	init	{

		QtGUI.palette = QPalette.dark;// light / system

		//server = Server.new(\Metronome, NetAddr("127.0.0.1", 57105));
		//server.makeWindow;
		server = Server.default;

		/*server.options.memSize = 2**20;
		server.options.device = "JackRouter";// use a specific soundcard*/

		server.waitForBoot({

			// Init Synth
			this.initSynthDef;

			// run the soft
			this.run;

		});

	}

	run {

		// Tempo
		bpmTempo = TempoClock.new.tempo = 120/60;
		// Beats
		beatsList = [];
		32.do({arg beat; beatsList = beatsList.add((beat + 1).asString)});
		// Bares
		baresList = [];
		32.do({arg bare; baresList = baresList.add((bare + 1).asString)});
		// Init PatternProxy
		freq = PatternProxy.new;
		amp = PatternProxy.new;
		dur = PatternProxy.new;
		// Tuning
		tuning = Tuning.et12;
		degrees = tuning.semitones;
		roots = 0;
		scale = Scale.new(((degrees + roots)%tuning.size).sort, tuning.size, tuning);
		bares = 4;
		beats = 4;
		tuningMode = ["- Tempered -", "Chromatic", "Whole Tone", "Major", "Minor", "Diminued", "Octatonic 1", "Octatonic 2", "Nonatonique", "Messiaen 4", "Messiaen 5", "Messiaen 6", "Messiaen 7", "Bi-Pentaphonic", "Major Pentatonic", "Minor Pentatonic", "Blues", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi", "- Indian Shrutis -", "22tet", "12tet", "Asavari", "Bhairava", "Bhairavi", "Bilaval", "Kafi", "Kalyan", "Khammaj", "Marava", "Pooravi", "Todi"];
		rootMode = [];
		22.do({arg i; rootMode =rootMode.add((i).asString)});

		//Fonction Create Pattern
		patternFonction = {
			var octave, ratio, degre, difL, difH, pos=(scale.degrees.size - 1);

			patternFreqStart = [];
			patternFreqEnd = [];
			patternFreq = [];
			patternAmp = [];
			patternDur = [];
			patternAmpStart = [];
			patternAmpEnd = [];
			// Freq
			beats.do({arg i; patternFreqStart = patternFreqStart.add((i * 5)%12)});
			beats.do({arg i; patternFreqEnd = patternFreqEnd.add((i * 7)%12)});
			patternFreqStart = patternFreqStart+60;
			patternFreqEnd = patternFreqEnd+60;
			(bares-1).do({arg i; patternFreq = patternFreq ++ patternFreqStart});
			patternFreq = patternFreq ++ patternFreqEnd;
			// Scaling
			patternFreq = patternFreq.collect({arg item, index;
				octave = item.midicps.cpsoct.round(0.001);
				ratio = octave.frac;
				octave= octave.floor;
				degre = (ratio * tuning.size + 0.5).floor;
				(scale.degrees.size - 1).do({arg i; var difL, difH;
					difL=abs(degre - scale.degrees.at(i));
					difH=abs(degre - scale.degrees.at(i+1));
					if(degre >= scale.degrees.at(i) and: {degre <= scale.degrees.at(i+1)},
						{if(difL <= difH, {pos = i},{pos = i+1})});
				});
				// Out Collect Item
				item = scale.degreeToFreq(pos, (octave + 1 * 12).midicps, 0);
			});
			beats.do({arg i; if(i == 0 , {patternAmpStart = patternAmpStart.add(0.9)}, {patternAmpStart = patternAmpStart.add(0.3)})});
			beats.do({arg i; if(i == 0 or: {i == (beats -1)} or: {i == (beats - 2)}, {patternAmpEnd = patternAmpEnd.add(0.9)}, {patternAmpEnd = patternAmpEnd.add(0.3)})});
			// Amp
			(bares -1).do({arg i; patternAmp = patternAmp ++ patternAmpStart});
			patternAmp = patternAmp ++ patternAmpEnd;
			// Dur
			beats.do({arg i; patternDur = patternDur.add(1)});
			// PatternProxy
			freq.source = Pseq(patternFreq, inf);
			amp.source = Pseq(patternAmp, inf);
			dur.source = Pseq(patternDur, inf);
		};

		this.creationGUI;

		bareMenu.valueAction_(4-1);
		beatMenu.valueAction_(4-1);
		tuningMenu.valueAction_(1);

		// Metronome
		metronome = Pbind.new(
			\instrument, \metronome,
			\freq, freq,
			\amp, amp,
			\dur, dur,
			\server, server,
		).play(bpmTempo);
		metronome.stop;

		// Stop programme
		cmdperiodfunc = {
			window.close;
			server.quit;
			server.remove;
		};

		CmdPeriod.doOnce(cmdperiodfunc);

	}

	creationGUI {

		// Creation ALL GUI

		window = Window.new("Metronome", Rect(500, 500, 500, 500), scroll: true);
		window.onClose = {
			menuMetronome.remove;// remove custom menu
			window.close;
			server.quit;
			server.window.close;
		};
		window.view.decorator = FlowLayout(window.view.bounds);
		// Start / Stop
		start = Button(window,Rect(0, 0, 100, 50)).states=[["Off", Color.black, Color.green],["On", Color.black, Color.red]];
		start.action = {arg action;
			if(action.value == 1, {metronome.reset; metronome.play}, {metronome.stop});
		};
		// Tempo
		bpm = EZSlider(window, 200 @ 50, "BPM", ControlSpec(15, 480, \exp, 1),
			{|bpm| bpmTempo.tempo = bpm.value / 60; metronome.reset}, 120,labelWidth: 60,numberWidth: 40);
		window.front;
		// Tap Tempo
		tapTempo = Button(window,Rect(0, 0, 100, 50)).states=[["Tap Tempo", Color.yellow, Color.red], ["Tap Tempo", Color.yellow, Color.green]];
		tapTempo.action = {arg tap;
			if(tap.value == 1, {tempo = Main.elapsedTime},
				{tempo = Main.elapsedTime - tempo;
					tempo = tempo.reciprocal * 60;	bpm.valueAction = tempo;
					metronome.reset;
			});
		};
		window.view.decorator.nextLine;
		// Bares
		StaticText(window, Rect(0, 0, 50, 20)).string_("Bares").stringColor_(Color.white(1, 1)).font_(Font("Georgia", 16));
		bareMenu = PopUpMenu(window, Rect(0, 0, 50, 20)).items = baresList;
		bareMenu.action = {arg bare;
			bares = bare.value + 1;
			metronome.reset;
			patternFonction.value;
		};
		// Beats
		StaticText(window, Rect(0, 0, 50, 20)).string_("Beats").stringColor_(Color.white(1, 1)).font_(Font("Georgia", 16));
		beatMenu = PopUpMenu(window, Rect(0, 0, 50, 20)).items = beatsList;
		beatMenu.action = {arg beat;
			beats = beat.value + 1;
			metronome.reset;
			patternFonction.value;
		};
		// Tuning
		StaticText(window, Rect(0, 0, 50, 20)).string_("Tuning").stringColor_(Color.white(1, 1)).font_(Font("Georgia", 16));
		tuningMenu = PopUpMenu(window, Rect(0, 0, 150, 20)).items = tuningMode;
		tuningMenu.action = {arg item;
			switch(item.value,
				// Tempered
				0, {nil},
				// Chromatic
				1, {degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]},
				// Whole Tone 1
				2, {degrees =  [0, 2, 4, 6, 8, 10]},
				// Major
				3, {degrees =  [0, 2, 4, 5, 7, 9, 11]},
				// Minor
				4, {degrees =  [0, 2, 3, 5, 7, 8, 10]},
				// Diminued
				5, {degrees =  [0, 2, 3, 5, 6, 8, 9, 11]},
				// Octatonic 1
				6, {degrees =  [0, 1, 3, 4, 6, 7, 9, 10]},
				// Octatonic 2
				7, {degrees =  [0, 2, 3, 5, 6, 8, 9, 11]},
				// Nonatonique
				8, {degrees =  [0, 2, 3, 4, 6, 7, 8, 10, 11]},
				// Messian 4
				9, {degrees =  [0, 1, 2, 5, 6, 7, 8, 11]},
				// Messian 5
				10, {degrees =  [0, 1, 5, 6, 7, 11]},
				// Messian 6
				11, {degrees =  [0, 2, 4, 5, 6, 8, 10, 11]},
				// Messian 7
				12, {degrees =  [0, 1, 2, 3, 5, 6, 7, 8, 9, 11]},
				// Bi-Pentaphonic
				13, {degrees =  [0, 1, 2, 4, 5, 6, 7, 9, 10, 11]},
				// Major Pentatonic
				14, {degrees =  [0, 2, 4, 7, 9]},
				// Minor Pentatonic
				15, {degrees =  [0, 3, 5, 7, 10]},
				// Blues
				16, {degrees =  [0, 3, 5, 6, 7, 10]},
				// Asavari
				17, {degrees =  [0, 2, 3, 5, 7, 8, 10]},
				// Bhairava
				17, {degrees =  [0, 1, 4, 5, 7, 8, 11]},
				// Bhairavi
				19, {degrees =  [0, 1, 3, 5, 7, 8, 10]},
				// Bilaval
				20, {degrees =  [0, 2, 4, 5, 7, 9, 11]},
				// Kafi
				21, {degrees =  [0, 2, 3, 5, 7, 9, 10]},
				// Kalyan
				22, {degrees =  [0, 2, 4, 6, 7, 9, 11]},
				// Khammaj
				23, {degrees =  [0, 2, 4, 5, 7, 9, 10]},
				// Marava
				24, {degrees =  [0, 1, 4, 6, 7, 9, 11]},
				// Pooravi
				25, {degrees =  [0, 1, 4, 6, 7, 8, 11]},
				// Todi
				26, {degrees =  [0, 1, 3, 6, 7, 8, 11]},
				// Indian Shrutis
				27, {nil},
				// 22tet
				28, {degrees =  [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21]},
				// 12tet
				29, {degrees =  [0, 2, 4, 6, 7, 9, 11, 13, 15, 16, 19, 20]},
				// Asavari
				30, {degrees =  [0, 4, 6, 9, 13, 15, 19]},
				// Bhairava
				31, {degrees =  [0, 2, 7, 9, 13, 15, 20]},
				// Bhairavi
				32, {degrees =  [0, 3, 5, 9, 13, 15, 18]},
				// Bilaval
				33, {degrees =  [0, 4, 7, 9, 13, 16, 20]},
				// Kafi
				34, {degrees =  [0, 4, 6, 9, 13, 16, 19]},
				// Kalyan
				35, {degrees =  [0, 4, 7, 11, 13, 16, 20]},
				// Khammaj
				36, {degrees =  [0, 4, 7, 9, 13, 16, 19]},
				// Marava
				37, {degrees =  [0, 2, 7, 11, 13, 16, 20]},
				// Pooravi
				38, {degrees =  [0, 2, 7, 11, 13, 15, 20]},
				// Todi
				39, {degrees =  [0, 2, 6, 11, 13, 15, 20]}
			);
			// Set Tuning
			if(item.value > 0 and: {item.value < 27}, {tuning = Tuning.et12; scale = Scale.new(((degrees + roots)%tuning.size).sort, tuning.size, tuning)});
			if(item.value > 27, {tuning = Tuning.sruti; scale = Scale.new(((degrees + roots)%tuning.size).sort, tuning.size, tuning)});
			metronome.reset;
			patternFonction.value;
			degreesMenu.value = degrees;
		};
		// Display Degrees
		degreesMenu = EZText(window, Rect(0, 0, 400, 20), "Degrees",
			{arg string;
				degrees = string.value;
				scale = Scale.new(((degrees + roots)%tuning.size).sort, tuning.size, tuning);
				metronome.reset;
				patternFonction.value;
			},
			[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11], true);
		window.view.decorator.nextLine;
		// Root
		StaticText(window, Rect(0, 0, 50, 20)).string_("Root").stringColor_(Color.white(1, 1)).font_(Font("Georgia", 16));
		rootMenu = PopUpMenu(window, Rect(0, 0, 50, 20)).items = rootMode;
		rootMenu.action = {arg i;
			roots = i.value;
			scale = Scale.new(((degrees + roots)%tuning.size).sort, tuning.size, tuning);
			metronome.reset;
			patternFonction.value;
		};
		window.view.decorator.nextLine;

		// Shortcuts
		fonctionShortCuts={arg window;
			window.view.keyDownAction = {arg view,char,modifiers,unicode, keycode;
				//[char,modifiers,unicode, keycode].postln;
				// esc
				if(modifiers == 0 and: {unicode == 27} and: {keycode == 53},
					{if(start.value == 1,
						{start.valueAction = 0},
						{start.valueAction = 1});
				});
			};
		};
		fonctionShortCuts.value(window);

	}

	initSynthDef {
		SynthDef(\metronome, { |freq = 440, amp = 1, dur = 1|
			var sig;
			sig = Saw.ar(freq, amp).distort;
			sig = sig * EnvGen.kr(Env.perc, doneAction: 2);
			Out.ar(0, sig ! 2);
		}).add;
	}

}
