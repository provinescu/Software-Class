// Launch Provinescu Software
Launcher {

	classvar  < s;

	var <> name, ni, o, r, f, w, devIn, devOut, listDevIn, listDevOut, path, nameSoft, driverBlock, widthMC, orientationMC, flagMC, wek, wekPort, scPort;

	*new	{arg path="~/Documents/", ni=2, o=2, r=2, f=0, devIn="Built-in Microph", devOut="Built-in Output", size = 512, wid=2.0, ori=0.5, flag=0, wek=6448, wekPort = 57120, scPort = 57110;

		^super.new.init(path, ni, o, r, f, devIn, devOut, size, wid, ori, flag, wek, wekPort, scPort);

	}

	init	{arg path, ni, o, r, f, devIn, devOut, size, wid, ori, flag, wek, wekPort, scPort;

		// Init
		QtGUI.palette = QPalette.dark;// light / system
		//Server.default = Server.internal;
		ni = 2;
		o = 2;
		r = 2;
		f = 0;
		listDevIn = ServerOptions.inDevices;
		listDevOut = ServerOptions.outDevices;
		devIn = listDevIn.at(0);
		devOut = listDevOut.at(0);
		driverBlock = 512;
		widthMC = 2.0;
		orientationMC = 0.5;
		flagMC = 0;
		wek = 6448;
		wekPort = 57120;
		scPort = 57110;

		// Output Panel
		w = Window("HP Software", Rect(666, 333, 210, 450), scroll: true);
		w.alpha=1.0;
		w.front;
		w.view.decorator = FlowLayout(w.view.bounds);

		//Server
		StaticText(w, Rect(0, 0, 50, 20)).string_("Server").stringColor_(Color.yellow);
		NumberBox(w, 50@20).value_(57110).action_{arg ez; scPort = ez.value.asInteger};
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
		//EZText(w, Rect(0, 0, 200, 20), "Size", {arg string; driverBlock = string.value}, driverBlock, true);
		NumberBox(w, 200@20).value_(driverBlock).action_{arg ez; driverBlock = ez.value.asInteger};

		//Text Input Number
		StaticText(w, Rect(0, 0, 200, 20)).string_("Channels Input").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		//EZText(w, Rect(0, 0, 200, 20), "Channels", {arg string; ni = string.value}, ni, true);
		NumberBox(w, 200@20).value_(ni).action_{arg ez; ni = ez.value.asInteger};

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
					SCRequestString("0", "Mode MultiCanals Static(0) Dynamic(1)", {arg strg; flagMC = strg.asInteger});
					//SCRequestString("2", "Channels", {arg strg; r = strg.asInteger; o = strg.asInteger; f = 2});
					f = 2;
				},
				3,	{
					SCRequestString("2.0", "Width", {arg strg; widthMC = strg.asFloat});
					SCRequestString("0.5", "Orientation", {arg strg; orientationMC = strg.asFloat});
					SCRequestString("0", "Mode MultiCanals Static(0) Dynamic(1)", {arg strg; flagMC = strg.asInteger});
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
		//EZText(w, Rect(0, 0, 200, 20), "Channels", {arg string; o = string.value; r = string.value}, o, true);
		NumberBox(w, 200@20).value_(o).action_{arg ez; o = ez.value.asInteger};

		//name = "Robot";
		nameSoft = "Robot";
		path = "~/Documents/Robot/";

		// Choose Software
		StaticText(w, Rect(0, 0, 200, 20)).string_("Choose Soft").stringColor_(Color.yellow);
		w.view.decorator.nextLine;
		PopUpMenu(w,Rect(0, 0, 200, 20)).items_(["Robot", "Agents", "Matrix", "Time", "Density", "Density2", "WekRobot", "WekAgents", "WekMatrix", "WekTime", "WekDensity", "WekDensity2", "Scores"]).stringColor_(Color.white).action = {|source|
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
				},
				5,	{
					nameSoft = "Density2";// 57568
					path = "~/Documents/Density2/";
				},
				6,	{
					w.view.children.at(14).string_("Choose Soft" + "(Wek Data 1)");
					nameSoft = "WekRobot";// 5770
					path = "~/Documents/WekRobot/";
					SCRequestString("57120", "Wek Out Port", {arg strg; wekPort = strg.asFloat});
					SCRequestString("6448", "Wek In Port", {arg strg; wek = strg.asFloat});
				},
				7,	{
					w.view.children.at(14).string_("Choose Soft" + "(Wek Data 95)");
					nameSoft = "WekAgents";// 5771
					path = "~/Documents/WekAgents/";
					SCRequestString("57120", "Wek Out Port", {arg strg; wekPort = strg.asFloat});
					SCRequestString("6448", "Wek In Port", {arg strg; wek = strg.asFloat});
				},
				8,	{
					w.view.children.at(14).string_("Choose Soft" + "(Wek Data 13)");
					nameSoft = "WekMatrix";// 57572
					path = "~/Documents/WekMatrix/";
					SCRequestString("57120", "Wek Out Port", {arg strg; wekPort = strg.asFloat});
					SCRequestString("6448", "Wek In Port", {arg strg; wek = strg.asFloat});
				},
				9,	{
					w.view.children.at(14).string_("Choose Soft" + "(Wek Data 71)");
					nameSoft = "WekTime";// 57573
					path = "~/Documents/WekTime/";
					SCRequestString("57120", "Wek Out Port", {arg strg; wekPort = strg.asFloat});
					SCRequestString("6448", "Wek In Port", {arg strg; wek = strg.asFloat});
				},
				10,	{
					w.view.children.at(14).string_("Choose Soft" + "(Wek Data 17)");
					nameSoft = "WekDensity";// 57574
					path = "~/Documents/WekDensity/";
					SCRequestString("57120", "Wek Out Port", {arg strg; wekPort = strg.asFloat});
					SCRequestString("6448", "Wek In Port", {arg strg; wek = strg.asFloat});
				},
				11,	{
					w.view.children.at(14).string_("Choose Soft" + "(Wek Data 17)");
					nameSoft = "WekDensity2";// 57574
					path = "~/Documents/WekDensity2/";
					SCRequestString("57120", "Wek Out Port", {arg strg; wekPort = strg.asFloat});
					SCRequestString("6448", "Wek In Port", {arg strg; wek = strg.asFloat});
				},
				12,	{
					w.view.children.at(14).string_("Choose Soft");
					nameSoft = "Scores";
				},
			);
		};

		//w.view.children.at(6).view.children.dump;

		//Text File In
		StaticText(w, Rect(0, 0, 200, 20)).string_("Choose Working Folder").stringColor_(Color.yellow);
		//Working Folder
		Button(w,Rect(0, 0, 200, 20)).states_([["Working Folder", Color.white]]).action = {arg start;
			FileDialog.new({ arg paths;
				paths = paths.at(0).asString ++"/"++ nameSoft;
				nameSoft.interpret.new(paths, ni, o, r, f, devIn, devOut, driverBlock, widthMC, orientationMC, flagMC, nameSoft, wek, wekPort, scPort);
				//Server.default.makeGui;
			},
			{
				nameSoft.interpret.new(path, ni, o, r, f, devIn, devOut, driverBlock, widthMC, orientationMC, flagMC, nameSoft, wek, wekPort, scPort);
				//Server.default.makeGui;
			}, fileMode: 2);
			w.close;
		};
		w.front;
	}

}