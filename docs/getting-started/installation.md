# Installation

## GitHub Binary Download

In most cases, the quickest way to install Jagr is by downloading the latest binary on the
[GitHub releases page](https://github.com/sourcegrade/jagr/releases).

## Arch-based Linux

The AUR package [jagr-bin](https://aur.archlinux.org/packages/jagr-bin) is available and may be installed with:

```shell
yay -S jagr-bin
```

## Build from source

In order to build Jagr from source, you must have git and Java version 11 or later installed.

### Clone the repository

<details open>
<summary>Via SSH</summary>

```shell
git clone git@github.com:sourcegrade/jagr.git
```
</details>

<details>
<summary>Via HTTPS</summary>

```shell
git clone https://github.com/sourcegrade/jagr.git
```
</details>

### Build the project

```shell
cd jagr
./gradlew build
```

The compiled jar will be located in `./build/libs/`.
