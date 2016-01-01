from django.db import models
from django.conf import settings
from django.contrib.auth.models import AbstractUser
from rest_framework.authtoken.models import Token




class Profile(AbstractUser):

    events_joined = models.ForeignKey('Event',related_name='events')


    @property
    def user_token(self):
        return Token.objects.get(user=self)

class Event(models.Model):

    creator = models.ForeignKey('Profile',related_name="creator")
    event_name = models.CharField(max_length=200)
