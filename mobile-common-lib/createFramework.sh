#!/bin/bash

echo ""
echo "Sample script. Modify paths etc before usage!"
echo ""
exit 1

set -e

DEFAULT_ARCHITECTURES=(arm64 armv7 x86_64)
TARGET_ARCHITECTURES=${ARCHS:-${DEFAULT_ARCHITECTURES[@]}}

FRAMEWORK_NAME="RiistaCommon"
SRC_FRAMEWORK_PATH="mobile-common-lib/output-framework/${FRAMEWORK_NAME}.framework"
SRC_FRAMEWORK_EXECUTABLE="${SRC_FRAMEWORK_PATH}/${FRAMEWORK_NAME}"
TARGET_FRAMEWORK_DIR="framework"

main()
{
    if [ "$#" -ne 1 ]; then
        printHelp
        exit 1
    fi

    MODE=$1
    if [[ "$MODE" != "thinned" ]] && [[ "$MODE" != "normal" ]]; then
        printHelp
        exit 1
    fi
    echo "Selected mode for framework: '$MODE'"


    echo "Creating $FRAMEWORK_NAME.framework containing all architectures.."
    ./gradlew :mobile-common-lib:createFatFramework

    if [[ -d $SRC_FRAMEWORK_PATH ]]
    then
        echo "$FRAMEWORK_NAME.framework created: $SRC_FRAMEWORK_PATH"

        if [[ "$MODE" == "thinned" ]]; then
            thinFramework
        fi

        copyFrameworkToTargetDir

        echo "---"
        echo "Framework created using mode '$MODE'"
    fi
}

printHelp()
{
    echo "Incorrect arguments: pass either 'thinned' or 'normal' as an argument"
    echo " 'thinned' - Script will generate a fat framework containing only the current architectures"
    echo " 'normal' -  Script will generate a fat framework containing armv7, arm64 and X86_64 architectures"
    echo ""
    echo "Current architectures is determined by ARCHS environment variable. If ARCHS is not"
    echo "present, the current architectures is assumed to contain armv7, arm64 and X86_64 architectures"
}

thinFramework()
{
    echo "Thinning created framework $SRC_FRAMEWORK_PATH."
    echo "  Target architectures: ${TARGET_ARCHITECTURES}"
    EXTRACTED_ARCHS=()

    for ARCH in $TARGET_ARCHITECTURES
    do
        echo "  Extracting ${ARCH} from ${SRC_FRAMEWORK_EXECUTABLE}"
        lipo -extract "$ARCH" "${SRC_FRAMEWORK_EXECUTABLE}" -o "$SRC_FRAMEWORK_EXECUTABLE-$ARCH"
        EXTRACTED_ARCHS+=("$SRC_FRAMEWORK_EXECUTABLE-$ARCH")
    done

    echo "  Merging extracted architectures: ${TARGET_ARCHITECTURES}"
    lipo -o "$SRC_FRAMEWORK_EXECUTABLE-merged" -create "${EXTRACTED_ARCHS[@]}"
    rm "${EXTRACTED_ARCHS[@]}"

    echo "  Replacing ${SRC_FRAMEWORK_EXECUTABLE} with thinned version"
    rm "$SRC_FRAMEWORK_EXECUTABLE"
    mv "$SRC_FRAMEWORK_EXECUTABLE-merged" "$SRC_FRAMEWORK_EXECUTABLE"
}

copyFrameworkToTargetDir()
{
    echo "Copying $SRC_FRAMEWORK_PATH to $TARGET_FRAMEWORK_DIR/"
    rm -rf $TARGET_FRAMEWORK_DIR
    mkdir $TARGET_FRAMEWORK_DIR
    mv $SRC_FRAMEWORK_PATH $TARGET_FRAMEWORK_DIR
}

main "$@"