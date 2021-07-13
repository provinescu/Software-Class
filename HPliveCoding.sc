// A Software by Herve Provini

HPliveCoding {

	*new	{arg textOnFly=0;

		^super.new.init(textOnFly);

	}

	init	{arg textOnFly;

		// Init GUI + edit Coding
		// utilise les variables de Agents !!!!

		~flagHPliveCoding = 'on';

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system

		this.edit(textOnFly);

	}

	edit {arg textOnFly;

		//Coding
		~wCoding = Window("Live Coding for Agents by HP", Rect(150, 150, 625, 500));
		~wCoding.view.decorator = FlowLayout(~wCoding.view.bounds);
		StaticText(~wCoding, Rect(0, 0, 500, 24)).string_("Live Coding Editor for Agents").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 14));
		~wCoding.view.decorator.nextLine;
		// Load Coding
		~menuCoding = PopUpMenu(~wCoding,Rect(0, 0, 175, 20)).background_(Color.grey(0.5, 0.8)).items = ["LiveCoding Menu", "Load CondingOnFly", "Save CondingOnFly"];
		~menuCoding.action={arg item, file;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load Coding
				1, {Dialog.getPaths({ arg paths;
					paths.do({ arg p; var file, coding;
						~wCoding.name="Live Coding Editor for Agents by HP"+p;
						~wEditCoding.open(p);
				})},{"cancelled".postln})},
				// Save Coding
				2, {Dialog.savePanel({arg path; var file;
					~wCoding.name=~nomFenetre+path;
					file=File(path++".scd","w");
					file.write(~wEditCoding.string);file.close;
				},{"cancelled".postln})}
			);
			~menuCoding.valueAction_(0);
		};
		~menuCoding.focus;
		// Evaluate Coding
		~validCoding = Button(~wCoding,Rect(0, 0, 150, 20)).states=[["Evaluate CodingOnFly", Color.black, Color.blue(0.8, 0.25)]];
		~validCoding.action = {arg view;
			~wEditCoding.string.interpret;
		};
		// Reset FX Coding
		~resetFX = Button(~wCoding,Rect(0, 0, 150, 20)).states=[["Reset FX", Color.black, Color.yellow(0.8, 0.25)]];
		~resetFX.action = {arg view;
			//~groupeSynthAgents.freeAll;
			~groupeEffets.freeAll;
			~busEffetsAudio.free;
			~busEffetsAudio=Bus.audio(Server.default, 2);
			~listSynthEffets=[];
			~listFX.size.do({arg effet;
				~listSynthEffets=~listSynthEffets.add(Synth.new(~listFX.wrapAt(effet).asString,['in', ~busEffetsAudio.index], ~groupeEffets, \addToTail));
				if(~playSynthEffets.wrapAt(effet) == 1, {~listSynthEffets.wrapAt(effet).run(true)}, {~listSynthEffets.wrapAt(effet).run(false)});
			});
			~listSynthEffets.size.do({arg effet;
				~listSynthEffets.wrapAt(effet).set('out', ~audioOutEffets.wrapAt(effet), 'amp', ~ampSynthEffets.wrapAt(effet).dbamp, 'pan', ~panSynthEffets.wrapAt(effet), 'control1', ~controlsSynthEffets.wrapAt(effet).wrapAt(0),  'control2', ~controlsSynthEffets.wrapAt(effet).wrapAt(1),  'control3', ~controlsSynthEffets.wrapAt(effet).wrapAt(2),  'control4', ~controlsSynthEffets.wrapAt(effet).wrapAt(3),  'control5', ~controlsSynthEffets.wrapAt(effet).wrapAt(4),  'control6', ~controlsSynthEffets.wrapAt(effet).wrapAt(5),  'control7', ~controlsSynthEffets.wrapAt(effet).wrapAt(6),  'control8', ~controlsSynthEffets.wrapAt(effet).wrapAt(7));
			});
		};
		~wCoding.view.decorator.nextLine;
		StaticText(~wCoding, Rect(0, 0, 175, 24)).string_("CodingOnFly").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 10));
		~wCoding.view.decorator.nextLine;
		~wEditCoding = TextView(~wCoding, Rect(0, 0, 600, 400));
		~wEditCoding.hasVerticalScroller_(true);
		~wEditCoding.hasHorizontalScroller_(true);
		~wEditCoding.autohidesScrollers_(true);
		~wEditCoding.resize_(5);
		~wEditCoding.string_("// Coding (bus ~busFileIn.index == 16)
Server.default.bind{
SynthDef('SynthCoding'.asString, {
arg out=0, inL=0, inR=1, buseffets, amp=0.3;
var synth;
synth = SoundIn.ar([inL, inR]);
//synth = In.ar([~busFileIn.index, ~busFileIn.index]); // FileIn Bus
//Out.ar([buseffets, out], synth * amp);
Out.ar(buseffets, synth * amp);
}).send(Server.default);
Server.default.sync;
~synthCoding = Synth.newPaused('SynthCoding'.asString, ['out', 0, 'inL', 0, 'inR', 1, 'buseffets', ~busEffetsAudio.index, 'amp', 1], ~groupeSynthAgents, 'addToTail');
Server.default.sync;
~synthCoding.run(true);
~synthCoding.set('amp', 0.1);
};
//~synthCoding.free;
");

		~wCoding.onClose_({~listewindows.removeAt(~positionWindow); ~indexwindow=0});

		~wCoding.front;

		//Setup Font
		~wCoding.view.do({arg view;
			view.children.do({arg subView;
				subView.font = Font("Helvetica", 12);
			});
		});

		//Init ShortCuts
		~listeWindows=~listeWindows.add(~wCoding);
		~positionWindow = ~listeWindows.size - 1;
		~fonctionShortCuts.value(~wCoding);

	}

}

