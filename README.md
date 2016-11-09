# [Komodo](https://komodoplatform.com/) Desktop GUI Wallet

NOTE: This is a copy of original project [zcash-swing-wallet-ui](https://github.com/vaklinov/zcash-swing-wallet-ui/). Only for educational purposes. This modified copy is made to work with komodod; expect it not to be stable and will not be maintanied either by the original developer or the person modified it.

## Graphical user interface wrapper for the [Komodo](https://komodoplatform.com/) command line tools

This program provides a Graphical User Interface (GUI) for the Komodo client tools that acts as a wrapper and 
presents the information in a user-friendly manner.

![Screenshot](https://github.com/satindergrewal/zcash-swing-wallet-ui/raw/master/docs/komodo-swing-wallet-gui.png "Main Window")

#### New/Experimental: [Komodo Desktop GUI Wallet for MAC OS X](https://github.com/vaklinov/zcash-swing-wallet-ui/blob/master/docs/Readme-Mac.md) is now available

## Building, installing and running the Wallet GUI

Before installing the Desktop GUI Wallet you need to have Komodo up and running. The following [guide](https://github.com/zcash/zcash/wiki/1.0-User-Guide) 
explains how to set up [Komodo](https://komodoplatform.com/). There is also a user-friendly [instructional video](https://www.youtube.com/watch?v=ZoRFLkZG0zg&feature=youtu.be)
on the same topic.

**For security reasons it is recommended to always build the GUI wallet program from GitHub**
**[source](https://github.com/vaklinov/zcash-swing-wallet-ui/archive/master.zip).**
The details of how to build it are described below (easy to follow).
Users who are less experienced with working on a command line, may instead use this 
quite-user-friendly [installation guide](https://www.cryptocompare.com/wallets/guides/how-to-install-the-zcash-gui-wallet) 
and [usage guide](https://www.cryptocompare.com/wallets/guides/how-to-use-the-zcash-gui-wallet).
The following video also explains how to [set up the GUI wallet](https://www.youtube.com/watch?v=IDifG4h1bgE). 
Users who insist on downloading a binary release may instead 
use [ZCash Desktop GUI Wallet - binary release v0.35-beta](https://github.com/vaklinov/zcash-swing-wallet-ui/blob/master/docs/Release_0.35-beta.md)


1. Operating system and tools

   As of November 2016 (ZCash v1.0.0) this program is only intended to work on Linux 
   (same limitation as [ZCash](https://z.cash/)). Future versions will work on MAC/Windows.  
   The Linux tools you need to build and run the Wallet GUI are Git, Java (JDK7 or later) and 
   Ant. If using Ubuntu Linux, they may be installed via command: 
   ```
   user@ubuntu:~/build-dir$ sudo apt-get install git default-jdk ant
   ``` 
   For RedHat/CentOS/Fedora-type Linux systems the command is (like):
   ```
   user@centos:~/build-dir$ sudo yum install java-1.8.0-openjdk git ant 
   ```
   The name of the JDK package (`java-1.8.0-openjdk`) may vary depending on the Linux system, so you need to
   check it, if name `java-1.8.0-openjdk` is not accepted.
   If you have some other Linux distribution, please check your relevant documentation on installing Git, 
   JDK and Ant. The commands `git`, `java`, `javac` and `ant` need to be startable from command line 
   before proceeding with build.

2. Building from source code

   As a start you need to clone the zcash-swing-wallet-ui Git repository:
   ```
   user@ubuntu:~/build-dir$ git clone https://github.com/satindergrewal/zcash-swing-wallet-ui.git
   ```
   Change the current directory:
   ```
   user@ubuntu:~/build-dir$ cd zcash-swing-wallet-ui/
   ```
   Issue the build command:
   ```
   user@ubuntu:~/build-dir/zcash-swing-wallet-ui$ ant -buildfile ./src/build/build.xml
   ```
   This takes a few seconds and when it finishes, it builds a JAR file `./build/jars/ZCashSwingWalletUI.jar`. 
   You need to make this file executable:
   ```
   user@ubuntu:~/build-dir/zcash-swing-wallet-ui$ chmod u+x ./build/jars/ZCashSwingWalletUI.jar
   ```
   At this point the build process is finished the built GUI wallet program is the JAR 
   file `./build/jars/ZCashSwingWalletUI.jar`

3. Installing the built ZCash GUI wallet

  3.1. If you have built ZCash from source code:

   Assuming you have already built from source code [ZCash](https://z.cash/) in directory `/home/user/komodo/src` (for 
   example - this is the typical build dir. for ZCash v1.0.0) which contains the command line tools `zcash-cli` 
   and `zcashd` you need to take the created file `./build/jars/ZCashSwingWalletUI.jar` and copy it 
   to diretcory `/home/user/komodo/src` (the same dir. that contains `zcash-cli` and `zcashd`). Example copy command:
   ```
   user@ubuntu:~/build-dir/zcash-swing-wallet-ui$ cp ./build/jars/ZCashSwingWalletUI.jar /home/user/komodo/src    
   ```
   
  3.2. If you have installed the ZCash [binary packages](https://github.com/zcash/zcash/wiki/Debian-binary-packages)

   The command line tools `zcash-cli` and `zcashd` are placed by the package installer in:
   ```
   /usr/bin/zcash-cli
   /usr/bin/zcashd
   ```
   The Komodo GUI wallet knows how to find them there. You may place the file  `./build/jars/ZCashSwingWalletUI.jar`
   anywhere in your `/home` directory that you find convenient and start it from there.

4. Running the installed Komodo GUI wallet

   Before running the GUI you need to start zcashd (e.g. `zcashd --daemon`). The wallet GUI is a Java program packaged 
   as an executable JAR file. It may be run from command line or started from another GUI tool (e.g. file manager). 
   Assuming you have already installed [ZCash](https://z.cash/) and the GUI Wallet `ZCashSwingWalletUI.jar` in 
   directory `/home/user/komodo/src` one way to run it from command line is:
   ```
   user@ubuntu:~/build-dir/zcash-swing-wallet-ui$ java -jar /home/user/komodo/src/ZCashSwingWalletUI.jar
   ```
   If you are using Ubuntu (or similar ;) Linux you may instead just use the file manager and 
   right-click on the `ZCashSwingWalletUI.jar` file and choose the option "Open with OpenJDK 8 Runtime". 
   This will start the Komodo GUI wallet.

### Donations accepted
At the present time this project is non-commercial in nature and developed by volunteers. If you find the GUI
Wallet useful, please consider making a donation for its further development. Your contribution matters! Donations 
are accepted at ZCash T address:
```
t1VAggo7RusLVBzHSeYbGkxDQQhLZyigxty
```

### License
This program is distributed under an [MIT License](https://github.com/vaklinov/zcash-swing-wallet-ui/raw/master/LICENSE).

### Disclaimer
This program is not officially endorsed by or associated with the ZCash project and the ZCash company.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

### Known issues and limitations

1. Limitation: Wallet encryption has been temporarily disabled in ZCash due to stability problems. A corresponding issue 
[#1552](https://github.com/zcash/zcash/issues/1552) has been opened by the ZCash developers. Correspondingly
wallet encryption has been temporarily disabled in the ZCash Desktop GUI Wallet.
1. Issue: the GUI wallet does not work correctly if zcashd is started with a custom data directory, like:
`zcashd -datadir=/home/data/whatever` This will be fixed in later versions.
1. Issue: GUI data tables (transactions/addresses etc.) allow copying of data via double click but also allow editing. 
The latter needs to be disabled. 
1. Limitation: The list of transactions does not show all outgoing ones (specifically outgoing Z address 
transactions). A corresponding issue [#1438](https://github.com/zcash/zcash/issues/1438) has been opened 
for the ZCash developers - soon to be fixed. A fix for the GUI wallet may be expected within 1-2 weeks. 
1. Limitation: The CPU percentage shown to be taken by zcashd is the average for the entire lifetime of the process. 
This is not very useful. This will be improved in future versions.
