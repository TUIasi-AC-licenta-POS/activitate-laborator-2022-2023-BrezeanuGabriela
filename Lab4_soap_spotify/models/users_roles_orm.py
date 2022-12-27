from sqlalchemy import Column, String, Integer, Date, Table, ForeignKey

from base.sql_base import Base

# user_roles_relationship = Table(
#     'users_roles', Base.metadata,
#     Column('user_id', Integer, ForeignKey('users.id'), primary_key=True),
#     Column('role_id', Integer, ForeignKey('roles.id'), primary_key=True),
#     extend_existing=True
# )

class Users_Roles(Base):
    __tablename__ = 'users_roles'

    user_id = Column(Integer, primary_key=True)
    role_id = Column(Integer, primary_key=True)

    def __init__(self, user_id, role_id):
        self.user_id = user_id
        self.role_id = role_id

