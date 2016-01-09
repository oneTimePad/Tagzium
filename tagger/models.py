from django.db import models
from django.conf import settings
from django.contrib.auth.models import AbstractUser
from rest_framework.authtoken.models import Token
from django.core.files.storage import FileSystemStorage
from random import random

STORAGE = '/home/lie/Desktop/Tagger/Images/'





class Profile(AbstractUser):

    @property
    def user_token(self):
        return Token.objects.get(user=self)

class Event(models.Model):
    event_name = models.CharField(max_length=200)
    creator = models.ForeignKey('Profile',related_name="creator")
    users = models.ManyToManyField(Profile,related_name='events_in')
    logo = models.ImageField(upload_to=upload,default=0)

class Picture(models.Model):
    photo = models.ImageField(upload_to=upload,default=0)
    event = models.ForeignKey('Event',default=0)
    users = models.ManyToManyField(Profile,related_name='pictures_in',default=0)
