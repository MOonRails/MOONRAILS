# MOonRAILS
A platform that uses space standards, convention over configuration and code generation to automate communication across devices.

# Requirements

JVM (Java 1.8)
Bash


# Why?

There are two stories from two very different programmers, both of them desperately need MOonRAILS but they don't know it.

User 1 is an amateur, he wants to play with a micro-controller. He like the idea of tinkering with hardware and has even bought some parts. But it has now been over an year since he first bought the hardware and he did very little else other than lighting some LEDs and measuring some devices. Don't get him wrong, User 1 is very motivated, it's just that it takes a lot to get started, even after reading the values from his Arduino he still has to go through plenty of hurdles to provide the code to the 'outside world'. He's actually spent most of his time writing parsers and now has a very simple app that allows him to plot the temperature. By now he feels he has learned a lot and doesn't really want to move onto another project, maybe he'll find another hobby and get back to tinkering once he has more time.
If only he knew about MOonRAILS and how much time he could have saved..

User 2 is a seasoned professional, she knows how to code and understands very well computer and software architecture. However, she's frustrated by all the bureaucracy that surrounds her job. You see, she's responsible for satellite flight software and she loves to write safe, reliable and easy to test code. Nevertheless she spends more and more time writing interface documents, defining where the bits and bytes will go. It seems to her that for each line of code, 10 lines of documentation are needed. Now don't get me wrong, User 2 is a very responsible engineer and she understands that need for clear, unambiguous documentation. She just wishes that she there was some better way that somehow would allow her to spend more time on doing the important stuff and less time on explaining it to others.
If only she knew about MOonRAILS and how much time she could be saving every day..


## When convention is key, everything else is free
It's a sound rhyme, but doesn't really make sense, until now.

You see, for some years now the [CCSDS](https://public.ccsds.org/default.aspx) organization has been defining the [new generation of space standards](https://en.wikipedia.org/wiki/CCSDS_MO_Services). They too have seen that the time of change has come. Lot's of open-source code [is being produced](https://github.com/esa/CCSDS_MO/wiki).

_Ok, that's very nice and such, but what does that have to do with Arduinos? Are you putting Arduinos in Space? Is this a tool for it?_

Jein. MOonRAILS is a platform for fast software development. Built on top of code conventions it uses code processing during build-time to generate all the crap nobody likes to do.. (_.. and yet we all know how important it is_).

This has been enabled by the latest standards from the [Consultative Committee for **_Space_** Data Systems](https://public.ccsds.org/default.aspx). While other standards could have been used with the same concept (e.g. IOT's [CoAP](http://coap.technology/)), we're space loving fans, so that was it.

## Finally some technical juice
If you're still reading, then probably you're ready for the technical part of this page.

Breathe deeply..

MOonRails framework uses Eclipse CDT's open-source libraries to parse your C/C++ code.
Based on that it creats an [Abstract Syntax Tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree) (not the one provided by CDT, but one destiled for its purpose), we call it the MOonRails abstraction or MORA ...just kiding we have [enough acronyms](https://twitter.com/davejohnson/status/602951117413216256).

From the MOonRails abstraction we can then generate all sorts of good stuff. And it's extensible too, meaning you can add your own plugins.

One built-in plugin is the MO XML generator. This is a very important one, because it takes your code and *_magic happens_* it's suddenly described in an [XML format](https://github.com/ESA/CCSDS_MO_XML) used to describe spacecraft interfaces (and other cool stuff).

Yes, you are now ready to take your "blinkLED" function into space. But it's no longer a _function_, it's an _operation_. Why? Because that's the convention's name for it.

But because you've now been automagically transported into the space domain, we can use all the goodies that comes with it.

One such goodie, is a standard way ([actually many](https://public.ccsds.org/Pubs/Forms/AllItems.aspx)) for encoding/decoding of communication messages. And... (drum roll) you don't need know to know anything about the standards way for moving packets around in an MOonRails world!

Why? Because MOonRails also generates the wrapping code. Yes, it reads your code, knows what you want to do because you're nice and follow the conventions, and then generates all the I/O handling bits and pieces.

Oh, and it also generates MS Word documentation if you need that type of stuff (but please remember to comment the code).

Yes, for all that you'll need plugins. Right now we have two:
- MO XML Plugin
- Arduino single file plugin

From the MO XML Plugin we can then access the generation of documentation and a [Java stubs](https://github.com/esa/CCSDS_MO_StubGenerator).
