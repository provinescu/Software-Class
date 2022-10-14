// Launch Provinescu Software
Launcher {

	classvar  < s;

	var <> name, ni, o, r, f, w, devIn, devOut, listDevIn, listDevOut, path, nameSoft, driverBlock, widthMC, orientationMC, flagMC;

	*new	{arg path="~/Documents/", ni=26, o=2, r=2, f=0, devIn="Built-in Microph", devOut="Built-in Output", size = 256, wid=2.0, ori=0.5, flag=0;

		^super.new.init(path, ni, o, r, f, devIn, devOut, size, wid, ori, flag);

	}

	init	{arg path, ni, o, r, f, devIn, devOut, size, wid, ori, flag;

		// Load Provinescu Software

		// Init
		QtGUI.palette = QPalette.dark;// light / system
		//Server.default = Server.internal;
		ni = 26;
		o = 2;
		r = 2;
		f = 0;
		listDevIn = ServerOptions.inDevices;
		listDevOut = ServerOptions.outDevices;
		devIn = listDevIn.at(0);
		devOut = listDevOut.at(0);
		driverBlock = 256;
		widthMC = 2.0;
		orientationMC = 0.5;
		flagMC = 0;

		// Output Panel
		w = Window("Launcher", Rect(666, 333, 210, 450), scroll: true);
		w.alpha=1.0;
		w.front;
		w.view.decorator = FlowLayout(w.view.bounds);

		//Server
		StaticText(w, Rect(0, 0, 200, 20)).string_("Server").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		PopUpMenu(w,Rect(0, 0, 200, 20)).items_(["Internal", "Local"]).stringColor_(Color.white).action = {|source| if(source.value == 0,
			{Server.default = Server.internal},
			{Server.default = Server.local});
		};

		//Text File In
		StaticText(w, Rect(0, 0, 200, 20)).string_("SoundCard (In / Out)").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		//Soundcard IN
		PopUpMenu(w,Rect(0, 0, 200, 20)).items_(ServerOptions.inDevices).stringColor_(Color.white).action = {|source| devIn = listDevIn.at(source.value)};
		//Soundcard OUT
		PopUpMenu(w,Rect(0, 0, 200, 20)).items_(ServerOptions.outDevices).stringColor_(Color.white).action = {|source| devOut = listDevOut.at(source.value)};

		//Text Driver Block
		StaticText(w, Rect(0, 0, 200, 20)).string_("Driver's Block Size").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		EZText(w, Rect(0, 0, 200, 20), "Size", {arg string; driverBlock = string.value}, 256, true);

		//Text Input Number
		StaticText(w, Rect(0, 0, 200, 20)).string_("Channels Input").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		EZText(w, Rect(0, 0, 200, 20), "Channels", {arg string; ni = string.value}, 26, true);

		//Format
		//Text File In
		StaticText(w, Rect(0, 0, 200, 20)).string_("Output Format").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		//Format
		PopUpMenu(w,Rect(0, 0, 200, 20)).items_(["Stereo", "Rotate2", "MultiSpeaker", "Ambisonic", "Dolby5.1"]).stringColor_(Color.white).action = {|source|
			switch (source.value,
				0, {
					//SCRequestString("2", "Channels", {arg strg; r = strg.asInteger; o = strg.asInteger; f = 0});
					f = 0;
				},
				1, {
					//SCRequestString("2", "Channels", {arg strg; r = strg.asInteger; o = strg.asInteger; f = 1;});
					f = 1;
				},
				2,	{
					SCRequestString("2.0", "Width", {arg strg; widthMC = strg.asFloat});
					SCRequestString("0.5", "Orientation", {arg strg; orientationMC = strg.asFloat});
					SCRequestString(0, "Mode MultiCanals Static(0) Dynamic(1)", {arg strg; flagMC = strg.asInteger});
					//SCRequestString("2", "Channels", {arg strg; r = strg.asInteger; o = strg.asInteger; f = 2});
					f = 2;
				},
				3,	{
					SCRequestString("2.0", "Width", {arg strg; widthMC = strg.asFloat});
					SCRequestString("0.5", "Orientation", {arg strg; orientationMC = strg.asFloat});
					SCRequestString(0, "Mode MultiCanals Static(0) Dynamic(1)", {arg strg; flagMC = strg.asInteger});
					//SCRequestString("2", "Channels", {arg strg; r = strg.asInteger; o = strg.asInteger; f = 3});
					f = 3;
				},
				4,	{
					//r = 6; o = 6;
					f = 4;
				}
			);
		};

		//Text Output Number
		StaticText(w, Rect(0, 0, 200, 20)).string_("Channels Output").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		EZText(w, Rect(0, 0, 200, 20), "Channels", {arg string; o = string.value; r = string.value}, o, true);

		//name = "Robot";
		nameSoft = "Robot";
		path = "~/Documents/Robot/";

		// Choose Software
		StaticText(w, Rect(0, 0, 200, 20)).string_("Choose Soft").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		PopUpMenu(w,Rect(0, 0, 200, 20)).items_(["Robot", "Agents", "Matrix", "Time", "Density"]).stringColor_(Color.white).action = {|source|
			switch (source.value,
				0, {
					nameSoft = "Robot";// 57564
					path = "~/Documents/Robot/";
				},
				1, {
					nameSoft = "Agents";// 57565
					path = "~/Documents/Agents/";
				},
				2,	{
					nameSoft = "Matrix";// 57566
					path = "~/Documents/Matrix/";
				},
				3,	{
					nameSoft = "Time";// 57567
					path = "~/Documents/Time/";
				},
				4,	{
					nameSoft = "Density";// 57568
					path = "~/Documents/Density/";
				}
			);
		};

		//Text File In
		StaticText(w, Rect(0, 0, 200, 20)).string_("Choose Working Folder").stringColor_(Color.yellow);
		//Working Folder
		Button(w,Rect(0, 0, 200, 20)).states_([["Working Folder", Color.white]]).action = {arg start;
			FileDialog.new({ arg paths;
				paths = paths.at(0).asString ++"/"++ nameSoft;
				nameSoft.interpret.new(paths, ni, o, r, f, devIn, devOut, driverBlock, widthMC, orientationMC, flagMC);
				//Server.default.makeGui;
			},
			{
				nameSoft.interpret.new(path, ni, o, r, f, devIn, devOut, driverBlock, widthMC, orientationMC, flagMC);
				//Server.default.makeGui;
			}, fileMode: 2);
			w.close;
		};
		w.front;
	}

}