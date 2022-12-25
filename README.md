Uhluhtc(WIP)
=======

This is a WIP NetHack monster information QQ bot plugin.(for mirai-console)

Bot commands
--------

    NetHack:            #?monster    (temporaily not available)
    NetHack:            #v?monster   (alias for just #?)
    UnNetHack:          #u?monster
    NetHack 3.4.3:      #V?monster
    UnNetHackPlus:      #u+?monster
    SporkHack:          #s?monster
    GruntHack:          #g?monster
    Slash'EM:           #l?monster
    SlashTHEM:          #lt?monster
    NetHack Brass:      #b?monster
    dNetHack:           #d?monster
    notdNethack:        #n?monster
    EvilHack:           #e?monster
    XNetHack:           #x?monster
    SpliceHack:         #sp?monster

How to run
----------

1. Grab monsterDB files from https://github.com/UnNetHack/pinobot/tree/master/variants
2. Put all the files into the plugin data folder.
3. Enjoy!

Known issues
----------

The latest mirai-core have dependency issues with Uhluhtc, so you have to **MANUALLY** overwrite mirai-core's `kotlinx.serialization` dependency with Uhluhtc's.