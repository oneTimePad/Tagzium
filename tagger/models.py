from django.db import models
from django.conf import settings
from django.contrib.auth.models import AbstractUser
from rest_framework.authtoken.models import Token




class Profile(AbstractUser):


    USERNAME_FIELD='username'

    @property
    def user_token(self):
        return Token.objects.get(user=self)

class Event(models.Model):
    event_name = models.CharField(max_length=200)
    creator = models.ForeignKey('Profile',related_name="creator")
    users = models.ManyToManyField(Profile,related_name='users')
