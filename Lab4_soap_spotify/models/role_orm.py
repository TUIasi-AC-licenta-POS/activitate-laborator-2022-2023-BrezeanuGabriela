from sqlalchemy import Column, String, Integer, Table, ForeignKey
from base.sql_base import Base

# movies_actors_association = Table(
#     'users_roles', Base.metadata,
#     Column('user_id', Integer, ForeignKey('users.id')),
#     Column('role_id', Integer, ForeignKey('roles.id'))
# )


class Role(Base):
    __tablename__ = 'roles'

    id = Column(Integer, primary_key=True)
    role_name = Column(String)

    def __init__(self, role_name):
        self.role_name = role_name
