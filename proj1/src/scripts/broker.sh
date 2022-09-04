#!/usr/bin/env bash

argc=$#
if (( argc != 1 ))
then
	echo "Usage: $0 <savingPeriod>"
	echo "If saving period is == 0, then the state is saved whenever a change is made. Otherwise, state is saved with a period of \$savingPeriod seconds."
	exit 1
fi

cd ..
gradle run -Pfile=pt/up/fe/sdle/reliableps/Publisher --args=$1
