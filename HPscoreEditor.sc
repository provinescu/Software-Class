// A Software by Herve Provini

HPscoreEditor {

	*new	{arg score=0;

		^super.new.init(score);

	}

	init	{arg score;

		// Init GUI + edit score
		// utilise les variables de AgentsBand !!!!

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system

		~flagHPscoreEditor = 'on';
		~flagHPscorePlaying = 'off';

		this.edit(score);

	}

	edit {arg score;

		//Score
		~wScore = Window("Score Editor for AgentsBand by HP", Rect(250, 250, 625, 500));
		~wScore.view.decorator = FlowLayout(~wScore.view.bounds);
		StaticText(~wScore, Rect(0, 0, 500, 24)).string_("A Score Editor for AgentsBand").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 14));
		~wScore.view.decorator.nextLine;
		// Load Score
		~menuScore = PopUpMenu(~wScore,Rect(0, 0, 175, 20)).background_(Color.grey(0.5, 0.8)).items = ["Score menu", "Load Score", "Save Score"];
		~menuScore.action={arg item, file;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load score
				1, {Dialog.getPaths({ arg paths;
					paths.do({ arg p; var file, score;
						~routineScore.value(~scorePlaying).stop;~routineScore.value(~scorePlaying).remove;
						file=File(p,"r");
						score = file.readAllString;
						~wScore.name="Score Editor for AgentsBand by HP"+p;
						~scorePlaying = score.interpret;
						~wEditScore.string_(score);
						file.close})},{"cancelled".postln})},
				// Save score
				2, {Dialog.savePanel({arg path; var file;
					~wScore.name=~nomFenetre+path;
					file=File(path++".scd","w");
					file.write(~wEditScore.string);file.close;
				},{"cancelled".postln})}
			);
			~menuScore.valueAction_(0);
		};
		~menuScore.focus;
		// Routine score display
		~startTdefScore = Button(~wScore,Rect(10, 10, 150, 18)).states=[["Score Play Off", Color.black, Color.green(0.8, 0.25)],["Score Play On", Color.black, Color.red(0.8, 0.25)]];
		~startTdefScore.action = {arg view;
			if (view.value == 1, {
				// Start Score
				~flagHPscorePlaying = 'on';
				if(~startsysteme.value == 1, {
					~tempoMusicPlay.schedAbs(~tempoMusicPlay.nextBar, {"Start Score".postln;~routineScore.value(~scorePlaying).reset;~routineScore.value(~scorePlaying).play;
						nil});
				},{"Start Score".postln;~routineScore.value(~scorePlaying).reset});
			}, {
				// Stop Score
				"Stop Score".postln;~routineScore.value(~scorePlaying).stop;~routineScore.value(~scorePlaying).clear;~flagHPscorePlaying = 'off';
			});
		};
		~validScore = Button(~wScore,Rect(0, 0, 150, 20)).states=[["Validation Score", Color.black, Color.blue(0.8, 0.25)]];
		~validScore.action = {arg view;
			~scorePlaying  = ~wEditScore.string.interpret;
		};
		~wScore.view.decorator.nextLine;
		StaticText(~wScore, Rect(0, 0, 175, 24)).string_("Score").stringColor_(Color.white(1.0,1.0)).font_(Font("Georgia", 10));
		~wScore.view.decorator.nextLine;
		~wEditScore = TextView(~wScore, Rect(0, 0, 600, 400));
		~wEditScore.hasVerticalScroller_(true);
		~wEditScore.hasHorizontalScroller_(true);
		~wEditScore.autohidesScrollers_(true);
		~wEditScore.resize_(5);
		~wEditScore.string_("[
[ 0.0417, 'End Score'.asString, [nil]]
]");

		~wScore.onClose_({~listewindows.removeAt(~positionWindow); ~indexwindow=0});

		~wScore.front;

		//Setup Font
		~wScore.view.do({arg view;
			view.children.do({arg subView;
				subView.font = Font("Helvetica", 12);
			});
		});

		//Init ShortCuts
		~listeWindows=~listeWindows.add(~wScore);
		~positionWindow = ~listeWindows.size - 1;
		~fonctionShortCuts.value(~wScore);

		// PROCESSUS Read Score
		~routineScore = {arg score;
			var time=0.0417 / ~tempoMusicPlay.tempo.reciprocal, cmd, val;
			Tdef(\ScoreEdit,
				{Routine{arg inval;
					score.do({arg item;
						if(~flagHPscorePlaying == 'on', {
							time = item.wrapAt(0);
							if(time * ~tempoMusicPlay.tempo.reciprocal < 0.0417, {time=0.0417 / ~tempoMusicPlay.tempo.reciprocal});
							cmd = item.wrapAt(1);
							val = item.wrapAt(2);
							if(cmd != "End Score", {
								if(cmd == "~evaluationKeyDown", {{cmd.interpret.value(val.wrapAt(0), val.wrapAt(1),val.wrapAt(2),val.wrapAt(3), val.wrapAt(4))}.defer},
									{{cmd.interpret.valueAction_(val)}.defer})},
							{{~startTdefScore.valueAction_(0)}.defer;
							});
						},
						{~routineScore.value(~scorePlaying).stop;~routineScore.value(~scorePlaying).remove;
							thisThread.stop;
							thisThread.remove});
						time.yield;
					});
				}.play;
			});
		};

	}

}

