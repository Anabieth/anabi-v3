# SAMS Data Warehouse

![logo](https://sams-project.eu/wp-content/uploads/2019/11/SAMS-DW.png)

SAMS is a project funded by the European Union within the H2020-ICT-39-2016-2017 call. 
SAMS enhances international cooperation of ICT (Information and Communication Technologies) 
and sustainable agriculture between EU and developing countries in pursuit of the EU commitment 
to the UN Sustainable Development Goal “End hunger, achieve food security and improved nutrition 
and promote sustainable agriculture”.
To get more information about the SAMS project please visit: https://sams-project.eu/

SAMS Data Warehouse (DW) is a universal system, which is able to operate with different
 data inputs and have flexible data processing algorithms. 

![architecture](front-end/src/assets/DW-concept-new.svg)

# SAMS DW Web API
This software is part of SAMS DW solution, it acts as a gateway between Internet and SAMS DW core 
providing RESTful API. This SAMS DW WebAPI handles request authentication, manages user workspaces, 
including beekeeping object registry, channels of incoming measurements, modelling setup.  
 
**SAMS DW User interface**

In addition, this software includes Web Application (front-end) which implements graphical
user interface for provided SAMS DW Web API. It covers whole management lifecycle,
from registering beekeeping objects and configuring hive monitoring devices 
to rendering reports (data-out). 

# Running local (development) environment
SAMS DW WebAPI by default requires MongoDB instance to be available on 
`localhost:27017` (can be changed in a configuration).

**Web API** is started by `lv.llu.science.bees.webapi.Application` class via IDE of your choice 
or by running 
```
./gradlew web-api:bootRun
```

Note for IntelliJ IDEA users: install `Lombok` plugin 
and enable annotation processing in `Settings > Build, Execution, Deployment > Compiler > Annotation Processors`.

**Front-end** is started as follows:
```
cd ./front-end
npm install     ;# if needed
npm start
```

# Building artifacts
```
./gradlew build
```
Compiled artifacts are available in `front-end/dist` and `web-api/build/libs` directories.

# Deployment and hosting

**NB!** Make sure to change configuration in system components if you plan to host 
your own public instance of SAMS DW system. 
By default, it uses settings https://sams.science.itf.llu.lv/.

It is recommended to use Docker platform for deployment of SAMS DW solution 
with all related services.

To build Docker image of SAMS DW Web API run
```
./gradlew web-api:dockerImage
```

If needed private image registry can be configured in `./docker-build.gradle` file, 
and images can be pushed to it as follows
 
```
./gradlew web-api:dockerPush
```

*Docker compose* utility might be helpful for maintaining containers of services.
An example of configuration is available in `./deploy/docker-compose.yml`.
Whole system can be started as follows: 
```
docker-compose -f deploy/docker-compose.yml up
```

**Front-end** Web application can be hosted as static files using any Web server. 
An example of Nginx server configuration is available in `./deploy/nginx.conf`.
