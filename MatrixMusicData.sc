// A Software by Herve Provini

MatrixMusicData {

	var w, wMenu, wInstrument, instrument, wAudio, audio, wBand, band, upDatePreset, wPreset, preset, musicData, cmdperiodfunc, source, target, si, sa, sb, ti, ta, tb;

	*new	{arg data=nil, i=0, a=0, b=0;

		^super.new.init(data, i, a, b);

	}

	init	{arg data, i, a, b;

		// Setup GUI style
		QtGUI.palette = QPalette.dark;// light / system

		this.edit(data, i, a, b);

	}

	edit {arg data, i, a, b, f;

		instrument = i;
		audio = a;
		band = b;
		source = [  1  ,  1  ,  0  ];
		target = [  1  ,  1  ,  0  ];
		if(data != nil,
			{
				f = File(data.standardizePath,"r");
				preset = f.readAllString.interpret;
				f.close;
				musicData = preset.last;
				preset.remove(preset.last);
			},
			{"Load a Preset".postln});

		w = Window("Matrix Music Data Editor", Rect(500, 100, 625, 530));
		w.view.decorator = FlowLayout(w.view.bounds);
		StaticText(w, Rect(0, 0, 500, 20)).string_("Matrix Music Data Editor (A software for editing Matrix (freeze) musical data)").stringColor_(Color.white(1.0,1.0));
		w.view.decorator.nextLine;

		// menu Sequence
		wMenu = PopUpMenu(w,Rect(0, 0, 150, 20)).background_(Color.green(0.5, 0.8)).items = ["Preset Menu", "Load Preset", "Save Preset"];
		wMenu.action={arg item, file, data;
			item.value.switch(
				// Do Nothing
				0, {nil},
				// Load Preset
				1, {
					Dialog.openPanel({ arg paths, file, seq;
						file=File(paths,"r");
						data = file.readAllString.interpret;file.close;
						preset = data;
						musicData = preset.last;// Freeze Music
						preset.remove(preset.last);// Remove Freeze Music
					},
					{"cancelled".postln})},
				// Save Preset
				2, {Dialog.savePanel({arg path, file, data;
					file=File(path++".scd","w");
					data = preset.copy;
					data = data.add(musicData);// Add Freeze Music
					file.write(data.asCompileString);
					file.close;
				},
				{"cancelled".postln});
			});
			wMenu.value_(0);
		};
		wMenu.focus;

		// wInstrument
		StaticText(w, Rect(0, 0, 50, 20)).string_("Instr").stringColor_(Color.yellow);
		wInstrument=NumberBox(w, 25 @ 20);
		wInstrument.value = 1;
		wInstrument.action={arg number;
			if(number.value < 1, {wInstrument.valueAction = 1});
			instrument = number.value - 1;// Numero de l'instrument
			upDatePreset.value([instrument, audio, band]);
		};
		wInstrument.valueAction = 1;

		// wAudio
		StaticText(w, Rect(0, 0, 50, 20)).string_("Audio").stringColor_(Color.yellow);
		wAudio=NumberBox(w, 25 @ 20);
		wAudio.value = 1;
		wAudio.action={arg number;
			if(number.value < 1, {wAudio.valueAction = 1});
			audio = number.value - 1;// Numero de l'audio
			upDatePreset.value([instrument, audio, band]);
		};
		wAudio.valueAction = 1;

		// wBand
		StaticText(w, Rect(0, 0, 50, 20)).string_("Band").stringColor_(Color.yellow);
		wBand=NumberBox(w, 25 @ 20);
		wBand.value = 1;
		wBand.action={arg number;
			if(number.value < 0, {wBand.valueAction = 0});
			if(number.value > 12, {wBand.valueAction = 12});
			band = number.value;// Numero de la band
			upDatePreset.value([instrument, audio, band]);
		};
		wBand.valueAction = 0;

		Button(w, Rect(0, 0, 175, 20)).
		states_([["Display (Freeze) Music", Color.black, Color.magenta]]).
		action_({arg but;
			upDatePreset.value([instrument, audio, band]);// Display music Data
		});
		w.view.decorator.nextLine;

		// Source
		// Instrument
		StaticText(w, Rect(0, 0, 90, 20)).string_("Source > Instr").stringColor_(Color.yellow);
		si=NumberBox(w, 25 @ 20);
		si.value = 1;
		si.action={arg number;
			if(number.value < 1, {si.valueAction = 1});
			source.put(0, number.value);// Numero de l'instrument
		};
		si.valueAction = 1;

		// Audio
		StaticText(w, Rect(0, 0, 40, 20)).string_("Audio").stringColor_(Color.yellow);
		sa=NumberBox(w, 25 @ 20);
		sa.value = 1;
		sa.action={arg number;
			if(number.value < 1, {sa.valueAction = 1});
			source.put(1, number.value);// Numero de l'audio
		};
		sa.valueAction = 1;

		// Band
		StaticText(w, Rect(0, 0, 40, 20)).string_("Band").stringColor_(Color.yellow);
		sb=NumberBox(w, 25 @ 20);
		sb.value = 1;
		sb.action={arg number;
			if(number.value < 0, {sb.valueAction = 0});
			if(number.value > 12, {sb.valueAction = 12});
			source.put(2, number.value);// Numero de la band
		};
		sb.valueAction = 0;

		// Target
		// Instrument
		StaticText(w, Rect(0, 0, 90, 20)).string_("Target > Instr").stringColor_(Color.yellow);
		ti=NumberBox(w, 25 @ 20);
		ti.value = 1;
		ti.action={arg number;
			if(number.value < 1, {ti.valueAction = 1});
			target.put(0, number.value);// Numero de l'instrument
		};
		ti.valueAction = 1;

		// Audio
		StaticText(w, Rect(0, 0, 40, 20)).string_("Audio").stringColor_(Color.yellow);
		ta=NumberBox(w, 25 @ 20);
		ta.value = 1;
		ta.action={arg number;
			if(number.value < 1, {ta.valueAction = 1});
			target.put(1, number.value);// Numero de l'audio
		};
		ta.valueAction = 1;

		// Band
		StaticText(w, Rect(0, 0, 40, 20)).string_("Band").stringColor_(Color.yellow);
		tb=NumberBox(w, 25 @ 20);
		tb.value = 1;
		tb.action={arg number;
			if(number.value < 0, {tb.valueAction = 0});
			if(number.value > 12, {tb.valueAction = 12});
			target.put(2, number.value);// Numero de la band
		};
		tb.valueAction = 0;


		/*// Source
		EZText(w, Rect(0, 0, 165, 20), "Source",
			{arg string;
				source = string.value;
		}, source, true, 60, 115).setColors(stringColor: Color.yellow).font = Font( "Helvetica", 16);

		// Target
		EZText(w, Rect(0, 0, 160, 20), "Target",
			{arg string;
				target = string.value;
		}, target, true, 40, 115).setColors(stringColor: Color.yellow).font = Font( "Helvetica", 16);*/

		// Copy
		Button(w, Rect(0, 0, 65, 20)).
		states_([["Copy S>T", Color.black, Color.magenta]]).
		action_({arg but;
			var i,a,b,freeze;
			i = source.at(0)-1;
			a = source.at(1)-1;
			b = source.at(2);
			// Copy Freeze Data
			freeze = musicData.at(i).at(a).at(b);//Freeze data instr, Audio, Band
			i = target.at(0)-1;
			a = target.at(1)-1;
			b = target.at(2);
			musicData.at(i).at(a).put(b, freeze);
		});
		w.view.decorator.nextLine;

		StaticText(w, Rect(0, 0, 600, 20)).string_("(Freeze) Music Data (Pitch (midi),  Amp (db),  duree,  BPM,  Centroid,  Flatness,  Energy,  Flux)").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		wPreset = TextView(w, Rect(0, 0, 600, 400));
		wPreset.hasVerticalScroller_(true);
		wPreset.hasHorizontalScroller_(true);
		wPreset.autohidesScrollers_(true);
		w.view.decorator.nextLine;

		Button(w, Rect(0, 0, 175, 20)).
		states_([["Put (Freeze) Music !", Color.black, Color.red]]).
		action_({arg but, music, data;
			music = wPreset.string.interpret;
			music.do({arg i;
				data = i.value;
				data.put(0, data.at(0).midicps);//Set MIDI to Fhz
				data.put(1, data.at(1).dbamp);//Set DB to amp
			});
			musicData.at(instrument).at(audio).put(band, music)});
		w.view.decorator.nextLine;

		// Update Music Data
		upDatePreset={arg array, music;
			music = musicData.at(array.at(0)).at(array.at(1)).at(array.at(2)).deepCopy;//Freeze
			music.do({arg i, data;
				data = i.value;
				data.put(0, data.at(0).cpsmidi);// Set Fhz to MIDI
				data.put(1, data.at(1).ampdb);// Set amp to DB
			});
			// Edit data for display
			music = music.asCompileString;
			music = music.replace("],", "],"++Char.nl++Char.nl);
			wPreset.setString(music, 0, 999999999);
		};

		w.onClose_({nil});

		w.front;

		cmdperiodfunc = {w.close};

		CmdPeriod.doOnce(cmdperiodfunc);

	}

}

