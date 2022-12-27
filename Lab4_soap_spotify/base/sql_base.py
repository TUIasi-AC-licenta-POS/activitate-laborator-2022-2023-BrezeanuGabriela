from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

Base = declarative_base()
#engine = create_engine('mariadb+mariadbconnector://remote-admin:passwdremote@192.168.56.10:3306/auth-db')#, echo=True)
engine = create_engine('mariadb://admin-db:password@192.168.56.10:3306/auth-db')#, echo=True)

Session = sessionmaker(bind=engine)

#Base.metadata.create_all(engine)
