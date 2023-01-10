Swat4DP initial setup
=====================

# Prerequisites
- Java
- Ant
- Maven

## Recommended
- Git client
- Eclipse JEE
- cURL

# Recommended Setup

1. download eclipse JEE release bundle and install it
2. download the latest ANT version and extract it to your preferred location
3. download and install cURL (if not yet available on your system)
	- for a Windows based system, I suggest to install GitForWindows (https://gitforwindows.org/)
3. start eclipse (use whatever workspace you like)
4. clone the swat4dp repository (https://github.com/schlagundrahm/swat4dp)
5. build the swat-dp-tools project
5. import the projects (Import... --> Projects from Git)
	- Swat4DP
	- swat-dp-ant-tools
	- swat-dp-pom
	- swat-dp-service-templates
	- swat-dp-tools
6. add the Ant view to your workspace
7. add the Swat4DP/build.xml file to 
8. create a directory named ".swat4dp" in your home directory
9. create a file named "build.properties" within the above directory containing the following properties:
	- swat.customer=<your prefix> (e.g. company or department abbreviation)
	- swat.<your prefix>.customer.home=${user.home}/<path to your repository>/${swat.customer}-swat4dp
10. run the Ant target *96-swat-create-infra*
	1. Specify the values for an initial swat4dp infrastructure setup:
		- customer prefix (taken form your .swat4dp/build.properties --> *swat.customer*)
		- project directory (taken form your .swat4dp/build.properties --> *swat.<your prefix>.customer.home*)
		- zone names (defaults to *dmz*) - an arbitrary comma-separated list of zone abbreviations (you need at least one)
		- environment names (defaults to *int,prd*) - an arbitrary comma-separated list of environment abbreviations (you need at least one)
		- device sets (defaults to *int,prd*) - an arbitrary comma-separated list of device set abbreviations (you need at least one)
			it makes sense to use the same abbreviations as you have defined for the environments, but you can name it differently
		- location of a Java keystore file or the location of your cURL executable
